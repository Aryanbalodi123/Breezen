#include <jni.h>
#include <string>
#include <vector>
#include <cstdint>
#include <unistd.h>
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

static void secure_zero(void* p, size_t n) {
    volatile uint8_t *ptr = (volatile uint8_t*)p;
    while (n--) *ptr++ = 0;
}

static inline uint8_t ror8(uint8_t x, unsigned int r) {
    return (uint8_t)((x >> r) | (x << (8 - r)));
}

// FNV-1a like checksum
static uint32_t checksum32(const uint8_t* data, size_t len) {
    uint32_t sum = 0x811c9dc5;
    for (size_t i=0; i<len; ++i) {
        sum ^= data[i];
        sum *= 0x01000193;
    }
    return sum;
}

static void decrypt_part(uint8_t* buf, size_t len, uint8_t xorKey, uint8_t rot) {
    for (size_t i=0; i<len; ++i) {
        uint8_t b = buf[i];
        b ^= xorKey;
        b = ror8(b, rot);
        buf[i] = b;
    }
}

static std::string join_parts(const std::vector<std::string>& parts) {
    std::string out;
    size_t total = 0;
    for (const auto &s: parts) total += s.size();
    out.reserve(total);
    for (const auto &s: parts) out += s;
    return out;
}

static void wipe_string(std::string &s) {
    if (!s.empty()) {
        secure_zero((void*)s.data(), s.size());
        s.clear();
    }
}

// ---------------------------- KEY STORAGE ----------------------------

// --- GEMINI ---
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
static const uint8_t GEM_XOR_KEYS[5] = { 0xfb, 0x25, 0x87, 0xd3, 0xcc };
static const uint8_t GEM_ROTS[5]     = { 5, 3, 1, 6, 5 };

// --- SUPABASE ---
static const uint8_t SUP_PART_0[] = { 0x23,0xc3,0x5a,0x4b,0x1b,0x32,0x13,0x43,0x72,0x43,0x5a,0x42,0xa2,0xdb,0x42,0x81,0x7a,0x43,0x42,0x93,0x42,0x7b,0x9a,0xa1,0x13,0x12,0x42,0xb9,0x42,0x53,0x8b,0xca,0xba,0x12,0x5a,0xc1,0x79,0x23,0xc3,0x5a,0x8b,0x13 };
static const size_t SUP_PART_0_LEN = 42;
static const uint8_t SUP_PART_1[] = { 0x91,0x68,0xf8,0x60,0xf8,0x74,0xb4,0xcc,0x3c,0x54,0xfc,0x38,0xe8,0x44,0xb4,0x34,0x10,0x78,0x90,0x78,0xe4,0x74,0xec,0x34,0xf8,0x78,0x85,0x78,0xe8,0x64,0xe8,0xcc,0x00,0x9c,0x9c,0xd0,0x91,0x34,0xec,0xd0,0xe4,0x9c };
static const size_t SUP_PART_1_LEN = 42;
static const uint8_t SUP_PART_2[] = { 0x41,0xe3,0x62,0x85,0xe2,0xe4,0x02,0x05,0x21,0x84,0x27,0x41,0x41,0xe6,0xe2,0x21,0xe2,0xa3,0x62,0xe8,0xa1,0x84,0xa5,0xe6,0x09,0xe6,0x62,0x07,0x61,0x83,0x89,0x49,0xe2,0x46,0xa7,0x86,0xc1,0xe4,0xc4,0xe5,0xe2,0x26 };
static const size_t SUP_PART_2_LEN = 42;
static const uint8_t SUP_PART_3[] = { 0xd6,0x24,0x43,0x94,0x34,0x06,0x43,0xa4,0x35,0xe5,0x53,0xa4,0xd7,0x65,0x47,0xe4,0xa6,0x15,0x33,0x46,0x44,0xe4,0x13,0xa4,0xd6,0x64,0x53,0x94,0x34,0xe4,0x23,0xa4,0xd6,0xe5,0xd7,0x94,0xf4,0x73,0x92,0x45,0xf6 };
static const size_t SUP_PART_3_LEN = 41;
static const uint8_t SUP_PART_4[] = { 0x38,0xb3,0x71,0xc9,0x60,0x38,0x49,0x90,0xa3,0x10,0x79,0x49,0x49,0x30,0xa0,0x88,0x41,0x51,0x31,0x89,0xcb,0xc8,0x49,0x50,0x58,0x01,0x30,0x71,0x91,0xd8,0x59,0x63,0x51,0x10,0xa9,0x30,0xc1,0x20,0x59,0x93,0x20 };
static const size_t SUP_PART_4_LEN = 41;
static const uint8_t SUP_XOR_KEYS[5] = { 0x08, 0x5d, 0xcf, 0x70, 0x0a };
static const uint8_t SUP_ROTS[5]     = { 3, 2, 5, 4, 3 };

// --- TELEGRAM ---
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
static const uint8_t TEL_XOR_KEYS[5] = { 0x71, 0xbc, 0x66, 0xa4, 0x9c };
static const uint8_t TEL_ROTS[5]     = { 4, 4, 7, 1, 6 };

// ---------------------------- SIGNATURE CHECK HELPERS ----------------------------

static std::string to_hex_no_sep(const uint8_t* data, size_t len) {
    char buf[3];
    std::string s;
    s.reserve(len * 2);
    for (size_t i = 0; i < len; ++i) {
        sprintf(buf, "%02X", data[i]);
        s.append(buf, 2);
    }
    return s;
}

static bool get_cert_sha256(JNIEnv* env, std::string &outSha) {
    // Basic JNI setup to call PackageManager to get signature
    jclass atClass = env->FindClass("android/app/ActivityThread");
    if (!atClass) return false;
    jmethodID currentAppMethod = env->GetStaticMethodID(atClass, "currentApplication", "()Landroid/app/Application;");
    if (!currentAppMethod) return false;
    jobject app = env->CallStaticObjectMethod(atClass, currentAppMethod);
    if (!app) return false;

    jclass contextClass = env->GetObjectClass(app);
    jmethodID getPackageManager = env->GetMethodID(contextClass, "getPackageManager", "()Landroid/content/pm/PackageManager;");
    jmethodID getPackageName = env->GetMethodID(contextClass, "getPackageName", "()Ljava/lang/String;");
    jobject pm = env->CallObjectMethod(app, getPackageManager);
    jstring packageName = (jstring) env->CallObjectMethod(app, getPackageName);

    jclass pmClass = env->GetObjectClass(pm);
    // 0x08000000 = GET_SIGNING_CERTIFICATES
    jmethodID getPkgInfo = env->GetMethodID(pmClass, "getPackageInfo", "(Ljava/lang/String;I)Landroid/content/pm/PackageInfo;");
    jobject pkgInfo = env->CallObjectMethod(pm, getPkgInfo, packageName, 0x08000000);
    if (!pkgInfo) return false;

    jclass pkgInfoClass = env->GetObjectClass(pkgInfo);
    jfieldID signingInfoField = env->GetFieldID(pkgInfoClass, "signingInfo", "Landroid/content/pm/SigningInfo;");
    jobject signingInfo = env->GetObjectField(pkgInfo, signingInfoField);

    if (!signingInfo) return false;

    jclass signingInfoClass = env->GetObjectClass(signingInfo);
    jmethodID getSigners = env->GetMethodID(signingInfoClass, "getApkContentsSigners", "()[Landroid/content/pm/Signature;");
    jobjectArray signatures = (jobjectArray) env->CallObjectMethod(signingInfo, getSigners);

    if (!signatures) return false;

    jobject firstSig = env->GetObjectArrayElement(signatures, 0);
    if (!firstSig) return false;

    jclass sigClass = env->GetObjectClass(firstSig);
    jmethodID toByteArray = env->GetMethodID(sigClass, "toByteArray", "()[B");
    jbyteArray certBytes = (jbyteArray) env->CallObjectMethod(firstSig, toByteArray);

    jsize len = env->GetArrayLength(certBytes);
    jbyte* data = env->GetByteArrayElements(certBytes, nullptr);

    jclass mdClass = env->FindClass("java/security/MessageDigest");
    jmethodID getInstance = env->GetStaticMethodID(mdClass, "getInstance", "(Ljava/lang/String;)Ljava/security/MessageDigest;");
    jstring algo = env->NewStringUTF("SHA-256");
    jobject md = env->CallStaticObjectMethod(mdClass, getInstance, algo);
    env->DeleteLocalRef(algo);

    jmethodID update = env->GetMethodID(mdClass, "update", "([B)V");
    jmethodID digest = env->GetMethodID(mdClass, "digest", "()[B");
    env->CallVoidMethod(md, update, certBytes);
    jbyteArray hashArray = (jbyteArray) env->CallObjectMethod(md, digest);

    env->ReleaseByteArrayElements(certBytes, data, JNI_ABORT);

    jsize hlen = env->GetArrayLength(hashArray);
    jbyte* hdata = env->GetByteArrayElements(hashArray, nullptr);
    outSha = to_hex_no_sep((const uint8_t*)hdata, (size_t)hlen);
    env->ReleaseByteArrayElements(hashArray, hdata, JNI_ABORT);

    return true;
}

static bool is_signature_allowed(JNIEnv* env, jobject context) {
    std::string sha;
    if (!get_cert_sha256(env, sha)) {
        return false;
    }

    // Your Release Key SHA-256 (from your keytool output)
    const char* ALLOWED_SHA = "F6EF7F25CA8E3B80758A2AA4164D7D60104A7DF204D92B900B3E9D48ED8EF18C";

    if (sha == ALLOWED_SHA) {
        return true;
    }

    ALOGW("Signature mismatch! Access denied. Got: %s", sha.c_str());
    return false;
}

// ---------------------------- ASSEMBLY ----------------------------

static std::string build_from_raw(const uint8_t* buf, size_t len) {
    std::string s;
    s.resize(len);
    if (len) memcpy(&s[0], buf, len);
    return s;
}

static std::string assemble_key_safe(const uint8_t* part_ptrs[], const size_t part_lens[], const uint8_t xorKeys[], const uint8_t rots[], int count, uint32_t expected_checksum) {
    std::vector<std::string> parts;
    parts.reserve(count);

    for (int i=0; i<count; ++i) {
        size_t L = part_lens[i];
        if (L == 0) { parts.emplace_back(""); continue; }
        uint8_t* buf = (uint8_t*)malloc(L);
        if (!buf) {
            for (auto &p : parts) wipe_string(p);
            return std::string();
        }
        memcpy(buf, part_ptrs[i], L);
        decrypt_part(buf, L, xorKeys[i], rots[i]);
        std::string s = build_from_raw(buf, L);
        secure_zero(buf, L);
        free(buf);
        parts.push_back(std::move(s));
    }

    std::string joined = join_parts(parts);
    uint32_t cs = checksum32((const uint8_t*)joined.data(), joined.size());
    if (expected_checksum != 0 && cs != expected_checksum) {
        // Tamper detected or data corruption
        for (auto &p : parts) wipe_string(p);
        wipe_string(joined);
        return std::string();
    }
    for (auto &p : parts) wipe_string(p);
    return joined;
}

// ---------------------------- JNI EXPORTS ----------------------------

extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_breezen_core_network_Keys_getGeminiKey(JNIEnv *env, jobject thiz, jobject context) {
    // STRICT CHECK: Returns empty string if signature doesn't match
    if (!is_signature_allowed(env, context)) return env->NewStringUTF("");

    const uint8_t* parts[] = { GEM_PART_0, GEM_PART_1, GEM_PART_2, GEM_PART_3, GEM_PART_4 };
    const size_t lens[] = { GEM_PART_0_LEN, GEM_PART_1_LEN, GEM_PART_2_LEN, GEM_PART_3_LEN, GEM_PART_4_LEN };
    const uint8_t xorKeys[] = { GEM_XOR_KEYS[0], GEM_XOR_KEYS[1], GEM_XOR_KEYS[2], GEM_XOR_KEYS[3], GEM_XOR_KEYS[4] };
    const uint8_t rots[] = { GEM_ROTS[0], GEM_ROTS[1], GEM_ROTS[2], GEM_ROTS[3], GEM_ROTS[4] };

    std::string key = assemble_key_safe(parts, lens, xorKeys, rots, 5, 0xd6853d9f);
    if (key.empty()) return env->NewStringUTF("");
    jstring out = env->NewStringUTF(key.c_str());
    wipe_string(key);
    return out;
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_breezen_core_network_Keys_getTelegramBotToken(JNIEnv *env, jobject thiz, jobject context) {
    // STRICT CHECK: Returns empty string if signature doesn't match
    if (!is_signature_allowed(env, context)) return env->NewStringUTF("");

    const uint8_t* parts[] = { TEL_PART_0, TEL_PART_1, TEL_PART_2, TEL_PART_3, TEL_PART_4 };
    const size_t lens[] = { TEL_PART_0_LEN, TEL_PART_1_LEN, TEL_PART_2_LEN, TEL_PART_3_LEN, TEL_PART_4_LEN };
    const uint8_t xorKeys[] = { TEL_XOR_KEYS[0], TEL_XOR_KEYS[1], TEL_XOR_KEYS[2], TEL_XOR_KEYS[3], TEL_XOR_KEYS[4] };
    const uint8_t rots[] = { TEL_ROTS[0], TEL_ROTS[1], TEL_ROTS[2], TEL_ROTS[3], TEL_ROTS[4] };

    std::string key = assemble_key_safe(parts, lens, xorKeys, rots, 5, 0xc9f274b4);
    if (key.empty()) return env->NewStringUTF("");
    jstring out = env->NewStringUTF(key.c_str());
    wipe_string(key);
    return out;
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_breezen_core_network_Keys_getSupabaseAnonKey(JNIEnv *env, jobject thiz, jobject context) {
    // STRICT CHECK: Returns empty string if signature doesn't match
    if (!is_signature_allowed(env, context)) return env->NewStringUTF("");

    const uint8_t* parts[] = { SUP_PART_0, SUP_PART_1, SUP_PART_2, SUP_PART_3, SUP_PART_4 };
    const size_t lens[] = { SUP_PART_0_LEN, SUP_PART_1_LEN, SUP_PART_2_LEN, SUP_PART_3_LEN, SUP_PART_4_LEN };
    const uint8_t xorKeys[] = { SUP_XOR_KEYS[0], SUP_XOR_KEYS[1], SUP_XOR_KEYS[2], SUP_XOR_KEYS[3], SUP_XOR_KEYS[4] };
    const uint8_t rots[] = { SUP_ROTS[0], SUP_ROTS[1], SUP_ROTS[2], SUP_ROTS[3], SUP_ROTS[4] };

    std::string key = assemble_key_safe(parts, lens, xorKeys, rots, 5, 0xbb9fb4ff);
    if (key.empty()) return env->NewStringUTF("");
    jstring out = env->NewStringUTF(key.c_str());
    wipe_string(key);
    return out;
}