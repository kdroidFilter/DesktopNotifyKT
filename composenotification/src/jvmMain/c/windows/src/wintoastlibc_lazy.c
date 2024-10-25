/**
 * Copyright (c) 2022-2023 Peter Zhigalov <peter.zhigalov@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

#if !defined (UNICODE)
#define UNICODE
#endif
#if !defined (_UNICODE)
#define _UNICODE
#endif
#if !defined (WTLC_BUILD_LIBRARY)
#define WTLC_BUILD_LIBRARY
#endif

#include "wintoastlibc.h"

#if !defined (WTLC_DLLNAME)
#define WTLC_DLLNAME "wintoastlibc.dll"
#endif

static HMODULE s_module = NULL;

#define LOAD(X) \
    HMODULE X = s_module; \
    if(!X) \
    { \
        X = LoadLibraryA(WTLC_DLLNAME); \
        if(!X) \
        { \
            MessageBoxA(NULL, "FAILED TO LOAD " WTLC_DLLNAME, "Error", MB_OK | MB_ICONERROR); \
            abort(); \
        } \
        s_module = X; \
    }

#define SYMCHECK(NAME, SYM) \
    if(!SYM) \
    { \
        MessageBoxA(NULL, "FAILED TO FIND PROC " NAME " IN " WTLC_DLLNAME, "Error", MB_OK | MB_ICONERROR); \
        abort(); \
    }

#define NOARG

#define FORWARD0_HELPER(NAME, RTYPE, RKEYW) \
    RTYPE WTLCAPI NAME (void) { \
        typedef RTYPE (WTLCAPI * t)(void); \
        t f; \
        LOAD(h) \
        f = (t)GetProcAddress(h, #NAME); \
        SYMCHECK(#NAME, f) \
        RKEYW f(); \
    }
#define FORWARD0(NAME, RTYPE) FORWARD0_HELPER(NAME, RTYPE, return)
#define FORWARD0NR(NAME) FORWARD0_HELPER(NAME, void, NOARG)

#define FORWARD1_HELPER(NAME, RTYPE, RKEYW, A1TYPE) \
    RTYPE WTLCAPI NAME (A1TYPE a1) { \
        typedef RTYPE (WTLCAPI * t)(A1TYPE); \
        t f; \
        LOAD(h) \
        f = (t)GetProcAddress(h, #NAME); \
        SYMCHECK(#NAME, f) \
        RKEYW f(a1); \
    }
#define FORWARD1(NAME, RTYPE, A1TYPE) FORWARD1_HELPER(NAME, RTYPE, return, A1TYPE)
#define FORWARD1NR(NAME, A1TYPE) FORWARD1_HELPER(NAME, void, NOARG, A1TYPE)

#define FORWARD2_HELPER(NAME, RTYPE, RKEYW, A1TYPE, A2TYPE) \
    RTYPE WTLCAPI NAME (A1TYPE a1, A2TYPE a2) { \
        typedef RTYPE (WTLCAPI * t)(A1TYPE, A2TYPE); \
        t f; \
        LOAD(h) \
        f = (t)GetProcAddress(h, #NAME); \
        SYMCHECK(#NAME, f) \
        RKEYW f(a1, a2); \
    }
#define FORWARD2(NAME, RTYPE, A1TYPE, A2TYPE) FORWARD2_HELPER(NAME, RTYPE, return, A1TYPE, A2TYPE)
#define FORWARD2NR(NAME, A1TYPE, A2TYPE) FORWARD2_HELPER(NAME, void, NOARG, A1TYPE, A2TYPE)

#define FORWARD3_HELPER(NAME, RTYPE, RKEYW, A1TYPE, A2TYPE, A3TYPE) \
    RTYPE WTLCAPI NAME (A1TYPE a1, A2TYPE a2, A3TYPE a3) { \
        typedef RTYPE (WTLCAPI * t)(A1TYPE, A2TYPE, A3TYPE); \
        t f; \
        LOAD(h) \
        f = (t)GetProcAddress(h, #NAME); \
        SYMCHECK(#NAME, f) \
        RKEYW f(a1, a2, a3); \
    }
#define FORWARD3(NAME, RTYPE, A1TYPE, A2TYPE, A3TYPE) FORWARD3_HELPER(NAME, RTYPE, return, A1TYPE, A2TYPE, A3TYPE)
#define FORWARD3NR(NAME, A1TYPE, A2TYPE, A3TYPE) FORWARD3_HELPER(NAME, void, NOARG, A1TYPE, A2TYPE, A3TYPE)

#define FORWARD8_HELPER(NAME, RTYPE, RKEYW, A1TYPE, A2TYPE, A3TYPE, A4TYPE, A5TYPE, A6TYPE, A7TYPE, A8TYPE) \
    RTYPE WTLCAPI NAME (A1TYPE a1, A2TYPE a2, A3TYPE a3, A4TYPE a4, A5TYPE a5, A6TYPE a6, A7TYPE a7, A8TYPE a8) { \
        typedef RTYPE (WTLCAPI * t)(A1TYPE, A2TYPE, A3TYPE, A4TYPE, A5TYPE, A6TYPE, A7TYPE, A8TYPE); \
        t f; \
        LOAD(h) \
        f = (t)GetProcAddress(h, #NAME); \
        SYMCHECK(#NAME, f) \
        RKEYW f(a1, a2, a3, a4, a5, a6, a7, a8); \
    }
#define FORWARD8(NAME, RTYPE, A1TYPE, A2TYPE, A3TYPE, A4TYPE, A5TYPE, A6TYPE, A7TYPE, A8TYPE) \
    FORWARD8_HELPER(NAME, RTYPE, return, A1TYPE, A2TYPE, A3TYPE, A4TYPE, A5TYPE, A6TYPE, A7TYPE, A8TYPE)
#define FORWARD8NR(NAME, A1TYPE, A2TYPE, A3TYPE, A4TYPE, A5TYPE, A6TYPE, A7TYPE, A8TYPE) \
    FORWARD8_HELPER(NAME, void, NOARG, A1TYPE, A2TYPE, A3TYPE, A4TYPE, A5TYPE, A6TYPE, A7TYPE, A8TYPE)

FORWARD1(WTLC_Template_Create, WTLC_Template *, WTLC_TemplateType)
FORWARD1NR(WTLC_Template_Destroy, WTLC_Template *)
FORWARD2NR(WTLC_Template_setFirstLine, WTLC_Template *, LPCWSTR)
FORWARD2NR(WTLC_Template_setSecondLine, WTLC_Template *, LPCWSTR)
FORWARD2NR(WTLC_Template_setThirdLine, WTLC_Template *, LPCWSTR)
FORWARD3NR(WTLC_Template_setTextField, WTLC_Template *, LPCWSTR, WTLC_TextField)
FORWARD2NR(WTLC_Template_setAttributionText, WTLC_Template *, LPCWSTR)
FORWARD2NR(WTLC_Template_setImagePath, WTLC_Template *, LPCWSTR)
FORWARD3NR(WTLC_Template_setImagePathWithCropHint, WTLC_Template *, LPCWSTR, WTLC_CropHint)
FORWARD3NR(WTLC_Template_setHeroImagePath, WTLC_Template *, LPCWSTR, BOOL)
FORWARD2NR(WTLC_Template_setAudioSystemFile, WTLC_Template *, WTLC_AudioSystemFile)
FORWARD2NR(WTLC_Template_setAudioPath, WTLC_Template *, LPCWSTR)
FORWARD2NR(WTLC_Template_setAudioOption, WTLC_Template *, WTLC_AudioOption)
FORWARD2NR(WTLC_Template_setDuration, WTLC_Template *, WTLC_Duration)
FORWARD2NR(WTLC_Template_setExpiration, WTLC_Template *, INT64)
FORWARD2NR(WTLC_Template_setScenario, WTLC_Template *, WTLC_Scenario)
FORWARD2NR(WTLC_Template_addAction, WTLC_Template *, LPCWSTR)
FORWARD1(WTLC_Template_textFieldsCount, size_t, WTLC_Template *)
FORWARD1(WTLC_Template_actionsCount, size_t, WTLC_Template *)
FORWARD1(WTLC_Template_hasImage, BOOL, WTLC_Template *)
FORWARD1(WTLC_Template_hasHeroImage, BOOL, WTLC_Template *)
FORWARD2(WTLC_Template_textField, LPCWSTR, WTLC_Template *, WTLC_TextField)
FORWARD2(WTLC_Template_actionLabel, LPCWSTR, WTLC_Template *, size_t)
FORWARD1(WTLC_Template_imagePath, LPCWSTR, WTLC_Template *)
FORWARD1(WTLC_Template_heroImagePath, LPCWSTR, WTLC_Template *)
FORWARD1(WTLC_Template_audioPath, LPCWSTR, WTLC_Template *)
FORWARD1(WTLC_Template_attributionText, LPCWSTR, WTLC_Template *)
FORWARD1(WTLC_Template_scenario, LPCWSTR, WTLC_Template *)
FORWARD1(WTLC_Template_expiration, INT64, WTLC_Template *)
FORWARD1(WTLC_Template_type, WTLC_TemplateType, WTLC_Template *)
FORWARD1(WTLC_Template_audioOption, WTLC_AudioOption, WTLC_Template *)
FORWARD1(WTLC_Template_duration, WTLC_Duration, WTLC_Template *)
FORWARD1(WTLC_Template_isToastGeneric, BOOL, WTLC_Template *)
FORWARD1(WTLC_Template_isInlineHeroImage, BOOL, WTLC_Template *)
FORWARD1(WTLC_Template_isCropHintCircle, BOOL, WTLC_Template *)
FORWARD0(WTLC_Instance_Create, WTLC_Instance *)
FORWARD1NR(WTLC_Instance_Destroy, WTLC_Instance *)
FORWARD0(WTLC_isCompatible, BOOL)
FORWARD0(WTLC_isSupportingModernFeatures, BOOL)
FORWARD0(WTLC_isWin10AnniversaryOrHigher, BOOL)
FORWARD1(WTLC_strerror, LPCWSTR, WTLC_Error)
FORWARD2(WTLC_initialize, BOOL, WTLC_Instance *, WTLC_Error *)
FORWARD1(WTLC_isInitialized, BOOL, WTLC_Instance *)
FORWARD2(WTLC_hideToast, BOOL, WTLC_Instance *, INT64)
FORWARD8(WTLC_showToast, INT64, WTLC_Instance *, WTLC_Template *, void *, WTLC_CB_toastActivated, WTLC_CB_toastActivatedAction, WTLC_CB_toastDismissed, WTLC_CB_toastFailed, WTLC_Error *)
FORWARD1NR(WTLC_clear, WTLC_Instance *)
FORWARD1(WTLC_createShortcut, WTLC_ShortcutResult, WTLC_Instance *)
FORWARD1(WTLC_appName, LPCWSTR, WTLC_Instance *)
FORWARD1(WTLC_appUserModelId, LPCWSTR, WTLC_Instance *)
FORWARD2NR(WTLC_setAppUserModelId, WTLC_Instance *, LPCWSTR)
FORWARD2NR(WTLC_setAppName, WTLC_Instance *, LPCWSTR)
FORWARD2NR(WTLC_setShortcutPolicy, WTLC_Instance *, WTLC_ShortcutPolicy)
