

#include <jni.h>
#include <string>
#include <vector>
#include <cstdint>
#include <unistd.h>
#include <fcntl.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/ptrace.h>
#include <android/log.h>
#include <errno.h>

#define LOG_TAG "secrets_native"
#define ALOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define ALOGW(...) __android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__)
#define ALOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// ---------------------------- UTIL --------------------------------

// safe memzero
static void secure_zero(void* p, size_t n) {
    volatile uint8_t *ptr = (volatile uint8_t*)p;
    while (n--) *ptr++ = 0;
}

// rotate right
static inline uint8_t ror8(uint8_t x, unsigned int r) {
    return (uint8_t)((x >> r) | (x << (8 - r)));
}

// rotate left
static inline uint8_t rol8(uint8_t x, unsigned int r) {
    return (uint8_t)((x << r) | (x >> (8 - r)));
}

// check for ptrace/debugger through TracerPid in /proc/self/status
static bool is_tracerpid_present() {
    FILE* f = fopen("/proc/self/status", "r");
    if (!f) return false;
    char line[256];
    while (fgets(line, sizeof(line), f)) {
        if (strncmp(line, "TracerPid:", 10) == 0) {
            int tracer = atoi(line + 10);
            fclose(f);
            return tracer != 0;
        }
    }
    fclose(f);
    return false;
}

// scan /proc/self/maps for suspicious strings (frida, gum-js-loop, frida-agent, frida-server)
static bool detect_instrumentation() {
    FILE* f = fopen("/proc/self/maps", "r");
    if (!f) return false;
    char buf[512];
    while (fgets(buf, sizeof(buf), f)) {
        if (strstr(buf, "frida") || strstr(buf, "gum-js-loop") || strstr(buf, "frida-agent") || strstr(buf, "frida-server")) {
            fclose(f);
            return true;
        }
    }
    fclose(f);
    return false;
}

// attempt to disable ptrace attach by calling ptrace(PTRACE_TRACEME) - if it fails, debugger likely present
static bool detect_ptrace_traceme() {
    errno = 0;
    long res = ptrace(PTRACE_TRACEME, 0, 0, 0);
    if (res == -1 && errno != 0) {
        return true;
    }
    // detach if successful
    ptrace(PTRACE_DETACH, 0, 0, 0);
    return false;
}

// perform quick sanity checks; returns true if environment suspicious
static bool environment_suspect() {

#ifdef DEBUG
    // Developer-friendly mode: allow everything
    return false;
#endif

    // Production security mode
    if (is_tracerpid_present()) return true;
    if (detect_instrumentation()) return true;
    if (detect_ptrace_traceme()) return true;

    return false;
}


// compute a quick checksum of bytes (FNV-1a like)
static uint32_t checksum32(const uint8_t* data, size_t len) {
    uint32_t sum = 0x811c9dc5;
    for (size_t i=0;i<len;++i) {
        sum ^= data[i];
        sum *= 0x01000193;
    }
    return sum;
}

// decrypt part: per-part scheme: each byte was rotated left by 'rot' then XORed by keyByte
static void decrypt_part(uint8_t* buf, size_t len, uint8_t xorKey, uint8_t rot) {
    for (size_t i=0;i<len;++i) {
        uint8_t b = buf[i];
        b ^= xorKey;
        b = ror8(b, rot); // reverse of rol used during encoding
        buf[i] = b;
    }
}

// join vector of strings
static std::string join_parts(const std::vector<std::string>& parts) {
    std::string out;
    size_t total = 0;
    for (const auto &s: parts) total += s.size();
    out.reserve(total);
    for (const auto &s: parts) out += s;
    return out;
}

// wipe string contents
static void wipe_string(std::string &s) {
    if (!s.empty()) {
        secure_zero((void*)s.data(), s.size());
        s.clear();
    }
}

// ---------------------------- KEY STORAGE (encoded arrays you provided) ----------------------------

// --- GEMINI parts (from your encode.py output)
static const uint8_t GEM_PART_0[] = { 0xd3, 0xd2, 0xb4, 0xd7, 0x91, 0xd4, 0x73, 0x33 };
static const size_t GEM_PART_0_LEN = 8;

static const uint8_t GEM_PART_1[] = { 0x6e, 0x8e, 0xbf, 0x46, 0xae, 0xdf, 0xe7, 0x06 };
static const size_t GEM_PART_1_LEN = 8;

static const uint8_t GEM_PART_2[] = { 0xe5, 0x37, 0xe3, 0x13, 0x77, 0x37, 0x1b, 0x03 };
static const size_t GEM_PART_2_LEN = 8;

static const uint8_t GEM_PART_3[] = { 0xc0, 0x01, 0x41, 0x42, 0x4d, 0x8d, 0xde, 0xc1 };
static const size_t GEM_PART_3_LEN = 8;

static const uint8_t GEM_PART_4[] = { 0x25, 0x26, 0x61, 0x21, 0x86, 0xcb, 0xe5 };
static const size_t GEM_PART_4_LEN = 7;

// per-part XOR keys and rotations for Gemini (from your output)
static const uint8_t GEM_XOR_KEYS[5] = { 0xfb, 0x25, 0x87, 0xd3, 0xcc };
static const uint8_t GEM_ROTS[5]     = { 5, 3, 1, 6, 5 };

// --- SUPABASE ANON parts (from your encode.py output)
static const uint8_t SUP_PART_0[] = {
        0x23,0xc3,0x5a,0x4b,0x1b,0x32,0x13,0x43,0x72,0x43,0x5a,0x42,0xa2,0xdb,0x42,0x81,
        0x7a,0x43,0x42,0x93,0x42,0x7b,0x9a,0xa1,0x13,0x12,0x42,0xb9,0x42,0x53,0x8b,0xca,
        0xba,0x12,0x5a,0xc1,0x79,0x23,0xc3,0x5a,0x8b,0x13
};
static const size_t SUP_PART_0_LEN = 42;

static const uint8_t SUP_PART_1[] = {
        0x91,0x68,0xf8,0x60,0xf8,0x74,0xb4,0xcc,0x3c,0x54,0xfc,0x38,0xe8,0x44,0xb4,0x34,
        0x10,0x78,0x90,0x78,0xe4,0x74,0xec,0x34,0xf8,0x78,0x85,0x78,0xe8,0x64,0xe8,0xcc,
        0x00,0x9c,0x9c,0xd0,0x91,0x34,0xec,0xd0,0xe4,0x9c
};
static const size_t SUP_PART_1_LEN = 42;

static const uint8_t SUP_PART_2[] = {
        0x41,0xe3,0x62,0x85,0xe2,0xe4,0x02,0x05,0x21,0x84,0x27,0x41,0x41,0xe6,0xe2,0x21,
        0xe2,0xa3,0x62,0xe8,0xa1,0x84,0xa5,0xe6,0x09,0xe6,0x62,0x07,0x61,0x83,0x89,0x49,
        0xe2,0x46,0xa7,0x86,0xc1,0xe4,0xc4,0xe5,0xe2,0x26
};
static const size_t SUP_PART_2_LEN = 42;

static const uint8_t SUP_PART_3[] = {
        0xd6,0x24,0x43,0x94,0x34,0x06,0x43,0xa4,0x35,0xe5,0x53,0xa4,0xd7,0x65,0x47,0xe4,
        0xa6,0x15,0x33,0x46,0x44,0xe4,0x13,0xa4,0xd6,0x64,0x53,0x94,0x34,0xe4,0x23,0xa4,
        0xd6,0xe5,0xd7,0x94,0xf4,0x73,0x92,0x45,0xf6
};
static const size_t SUP_PART_3_LEN = 41;

static const uint8_t SUP_PART_4[] = {
        0x38,0xb3,0x71,0xc9,0x60,0x38,0x49,0x90,0xa3,0x10,0x79,0x49,0x49,0x30,0xa0,0x88,
        0x41,0x51,0x31,0x89,0xcb,0xc8,0x49,0x50,0x58,0x01,0x30,0x71,0x91,0xd8,0x59,0x63,
        0x51,0x10,0xa9,0x30,0xc1,0x20,0x59,0x93,0x20
};
static const size_t SUP_PART_4_LEN = 41;

// per-part XOR keys and rotations for SUPABASE (from your output)
static const uint8_t SUP_XOR_KEYS[5] = { 0x08, 0x5d, 0xcf, 0x70, 0x0a };
static const uint8_t SUP_ROTS[5]     = { 3, 2, 5, 4, 3 };

// --- TELEGRAM parts (from your encode.py output)
static const uint8_t TEL_PART_0[] = { 0x02,0x02,0x62,0x02,0x42,0x52,0x42,0x52,0x42,0x22 };
static const size_t TEL_PART_0_LEN = 10;

static const uint8_t TEL_PART_1[] = { 0x1f,0xa8,0xa8,0xd8,0x7a,0x8a,0x2f,0xf9,0xf9 };
static const size_t TEL_PART_1_LEN = 9;

static const uint8_t TEL_PART_2[] = { 0x45,0xfa,0xfe,0xff,0xfd,0x4b,0xde,0xfe,0x4a };
static const size_t TEL_PART_2_LEN = 9;

static const uint8_t TEL_PART_3[] = { 0xcc,0xc2,0x32,0x40,0x66,0x40,0x4e,0x20,0x06 };
static const size_t TEL_PART_3_LEN = 9;

static const uint8_t TEL_PART_4[] = { 0x10,0x0a,0x0e,0x86,0x4c,0x0f,0x4d,0x00,0x90 };
static const size_t TEL_PART_4_LEN = 9;

// per-part XOR keys and rotations for TELEGRAM (from your output)
static const uint8_t TEL_XOR_KEYS[5] = { 0x71, 0xbc, 0x66, 0xa4, 0x9c };
static const uint8_t TEL_ROTS[5]     = { 4, 4, 7, 1, 6 };

// Optional junk part (unused) to confuse static scanners
static const uint8_t JUNK_PART[] = { 0xAA, 0xBB, 0xCC, 0xDD };

// ---------------------------- ASSEMBLY + CHECKS ----------------------------

// helper to build a std::string from raw bytes (will copy and then zero temp)
static std::string build_from_raw(const uint8_t* buf, size_t len) {
    std::string s;
    s.resize(len);
    if (len) memcpy(&s[0], buf, len);
    return s;
}

// assemble function that performs all checks and returns the final key; returns empty on suspicion
static std::string assemble_key_safe(const uint8_t* part_ptrs[], const size_t part_lens[], const uint8_t xorKeys[], const uint8_t rots[], int count, uint32_t expected_checksum) {
    // anti-debug/anti-frida quick checks
    if (environment_suspect()) {
        ALOGW("Environment suspicious - aborting key assembly");
        return std::string();
    }

    std::vector<std::string> parts;
    parts.reserve(count);

    for (int i=0;i<count;++i) {
        size_t L = part_lens[i];
        if (L == 0) { parts.emplace_back(""); continue; }

        // copy encrypted bytes to mutable buffer
        uint8_t* buf = (uint8_t*)malloc(L);
        if (!buf) {
            ALOGE("malloc failed");
            // wipe collected parts
            for (auto &p : parts) wipe_string(p);
            return std::string();
        }
        memcpy(buf, part_ptrs[i], L);

        // decrypt in-place
        decrypt_part(buf, L, xorKeys[i], rots[i]);

        // build string and append
        std::string s = build_from_raw(buf, L);
        secure_zero(buf, L);
        free(buf);

        parts.push_back(std::move(s));
    }

    // join
    std::string joined = join_parts(parts);

    // checksum verify
    uint32_t cs = checksum32((const uint8_t*)joined.data(), joined.size());
    // expected checksums from your encode.py outputs:
    // GEM:   0xd6853d9f
    // SUP:   0xbb9fb4ff
    // TEL:   0xc9f274b4
    if (expected_checksum != 0 && cs != expected_checksum) {
        // checksum mismatch - possible tamper
        ALOGW("Checksum mismatch (got %08x expected %08x). Aborting.", cs, expected_checksum);
        // wipe
        for (auto &p : parts) wipe_string(p);
        wipe_string(joined);
        return std::string();
    }

    // wipe temp parts
    for (auto &p : parts) wipe_string(p);

    // return assembled key (caller is responsible for clearing it after use)
    return joined;
}

// ---------------------------- JNI EXPORTS ----------------------------

extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_breezen_core_network_Keys_getGeminiKey(JNIEnv *env, jobject thiz) {
    const uint8_t* parts[] = { GEM_PART_0, GEM_PART_1, GEM_PART_2, GEM_PART_3, GEM_PART_4 };
    const size_t lens[] = { GEM_PART_0_LEN, GEM_PART_1_LEN, GEM_PART_2_LEN, GEM_PART_3_LEN, GEM_PART_4_LEN };
    const uint8_t xorKeys[] = { GEM_XOR_KEYS[0], GEM_XOR_KEYS[1], GEM_XOR_KEYS[2], GEM_XOR_KEYS[3], GEM_XOR_KEYS[4] };
    const uint8_t rots[] = { GEM_ROTS[0], GEM_ROTS[1], GEM_ROTS[2], GEM_ROTS[3], GEM_ROTS[4] };

    const uint32_t EXPECTED_CHECKSUM = 0xd6853d9f;

    std::string key = assemble_key_safe(parts, lens, xorKeys, rots, 5, EXPECTED_CHECKSUM);
    if (key.empty()) {
        return env->NewStringUTF("");
    }
    jstring out = env->NewStringUTF(key.c_str());
    wipe_string(key);
    return out;
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_breezen_core_network_Keys_getTelegramBotToken(JNIEnv *env, jobject thiz) {
    const uint8_t* parts[] = { TEL_PART_0, TEL_PART_1, TEL_PART_2, TEL_PART_3, TEL_PART_4 };
    const size_t lens[] = { TEL_PART_0_LEN, TEL_PART_1_LEN, TEL_PART_2_LEN, TEL_PART_3_LEN, TEL_PART_4_LEN };
    const uint8_t xorKeys[] = { TEL_XOR_KEYS[0], TEL_XOR_KEYS[1], TEL_XOR_KEYS[2], TEL_XOR_KEYS[3], TEL_XOR_KEYS[4] };
    const uint8_t rots[] = { TEL_ROTS[0], TEL_ROTS[1], TEL_ROTS[2], TEL_ROTS[3], TEL_ROTS[4] };

    const uint32_t EXPECTED_CHECKSUM = 0xc9f274b4;

    std::string key = assemble_key_safe(parts, lens, xorKeys, rots, 5, EXPECTED_CHECKSUM);
    if (key.empty()) {
        return env->NewStringUTF("");
    }
    jstring out = env->NewStringUTF(key.c_str());
    wipe_string(key);
    return out;
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_breezen_core_network_Keys_getSupabaseAnonKey(JNIEnv *env, jobject thiz) {
    const uint8_t* parts[] = { SUP_PART_0, SUP_PART_1, SUP_PART_2, SUP_PART_3, SUP_PART_4 };
    const size_t lens[] = { SUP_PART_0_LEN, SUP_PART_1_LEN, SUP_PART_2_LEN, SUP_PART_3_LEN, SUP_PART_4_LEN };
    const uint8_t xorKeys[] = { SUP_XOR_KEYS[0], SUP_XOR_KEYS[1], SUP_XOR_KEYS[2], SUP_XOR_KEYS[3], SUP_XOR_KEYS[4] };
    const uint8_t rots[] = { SUP_ROTS[0], SUP_ROTS[1], SUP_ROTS[2], SUP_ROTS[3], SUP_ROTS[4] };

    const uint32_t EXPECTED_CHECKSUM = 0xbb9fb4ff;

    std::string key = assemble_key_safe(parts, lens, xorKeys, rots, 5, EXPECTED_CHECKSUM);
    if (key.empty()) {
        return env->NewStringUTF("");
    }
    jstring out = env->NewStringUTF(key.c_str());
    wipe_string(key);
    return out;
}
