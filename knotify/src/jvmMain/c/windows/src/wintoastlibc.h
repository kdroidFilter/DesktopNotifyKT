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

#if !defined (WINTOASTLIBC_H_INCLUDED)
#define WINTOASTLIBC_H_INCLUDED

#include <windows.h>
#include <string.h>

#if !defined (WTLC_BUILD_STATIC)
#if defined (WTLC_BUILD_LIBRARY)
#define WTLC_LIB __declspec(dllexport)
#else
#define WTLC_LIB __declspec(dllimport)
#endif
#else
#define WTLC_LIB
#endif

#if !defined (WTLCAPI)
#define WTLCAPI __cdecl
#endif

#if !defined (_In_)
#define _In_
#endif
#if !defined (_Out_)
#define _Out_
#endif
#if !defined (_In_opt_)
#define _In_opt_
#endif
#if !defined (_Out_opt_)
#define _Out_opt_
#endif

#if defined (__cplusplus)
extern "C" {
#endif

enum _WTLC_DismissalReason
{
    WTLC_DismissalReason_UserCanceled = 0,
    WTLC_DismissalReason_ApplicationHidden = 1,
    WTLC_DismissalReason_TimedOut = 2
};
typedef enum _WTLC_DismissalReason WTLC_DismissalReason;

typedef void (WTLCAPI * WTLC_CB_toastActivated)(void * userData);
typedef void (WTLCAPI * WTLC_CB_toastActivatedAction)(void * userData, int actionIndex);
typedef void (WTLCAPI * WTLC_CB_toastDismissed)(void * userData, WTLC_DismissalReason state);
typedef void (WTLCAPI * WTLC_CB_toastFailed)(void * userData);

enum _WTLC_Scenario
{
    WTLC_Scenario_Default,
    WTLC_Scenario_Alarm,
    WTLC_Scenario_IncomingCall,
    WTLC_Scenario_Reminder
};
typedef enum _WTLC_Scenario WTLC_Scenario;

enum _WTLC_Duration
{
    WTLC_Duration_System,
    WTLC_Duration_Short,
    WTLC_Duration_Long
};
typedef enum _WTLC_Duration WTLC_Duration;

enum _WTLC_AudioOption
{
    WTLC_AudioOption_Default = 0,
    WTLC_AudioOption_Silent,
    WTLC_AudioOption_Loop
};
typedef enum _WTLC_AudioOption WTLC_AudioOption;

enum _WTLC_TextField
{
    WTLC_TextField_FirstLine = 0,
    WTLC_TextField_SecondLine,
    WTLC_TextField_ThirdLine
};
typedef enum _WTLC_TextField WTLC_TextField;

enum _WTLC_TemplateType
{
    WTLC_TemplateType_ImageAndText01 = 0,
    WTLC_TemplateType_ImageAndText02 = 1,
    WTLC_TemplateType_ImageAndText03 = 2,
    WTLC_TemplateType_ImageAndText04 = 3,
    WTLC_TemplateType_Text01 = 4,
    WTLC_TemplateType_Text02 = 5,
    WTLC_TemplateType_Text03 = 6,
    WTLC_TemplateType_Text04 = 7
};
typedef enum _WTLC_TemplateType WTLC_TemplateType;

enum _WTLC_AudioSystemFile
{
    WTLC_AudioSystemFile_DefaultSound,
    WTLC_AudioSystemFile_IM,
    WTLC_AudioSystemFile_Mail,
    WTLC_AudioSystemFile_Reminder,
    WTLC_AudioSystemFile_SMS,
    WTLC_AudioSystemFile_Alarm,
    WTLC_AudioSystemFile_Alarm2,
    WTLC_AudioSystemFile_Alarm3,
    WTLC_AudioSystemFile_Alarm4,
    WTLC_AudioSystemFile_Alarm5,
    WTLC_AudioSystemFile_Alarm6,
    WTLC_AudioSystemFile_Alarm7,
    WTLC_AudioSystemFile_Alarm8,
    WTLC_AudioSystemFile_Alarm9,
    WTLC_AudioSystemFile_Alarm10,
    WTLC_AudioSystemFile_Call,
    WTLC_AudioSystemFile_Call1,
    WTLC_AudioSystemFile_Call2,
    WTLC_AudioSystemFile_Call3,
    WTLC_AudioSystemFile_Call4,
    WTLC_AudioSystemFile_Call5,
    WTLC_AudioSystemFile_Call6,
    WTLC_AudioSystemFile_Call7,
    WTLC_AudioSystemFile_Call8,
    WTLC_AudioSystemFile_Call9,
    WTLC_AudioSystemFile_Call10
};
typedef enum _WTLC_AudioSystemFile WTLC_AudioSystemFile;

enum _WTLC_CropHint
{
    WTLC_CropHint_Square,
    WTLC_CropHint_Circle
};
typedef enum _WTLC_CropHint WTLC_CropHint;

typedef struct _WTLC_Template WTLC_Template;

WTLC_LIB WTLC_Template * WTLCAPI WTLC_Template_Create(_In_ WTLC_TemplateType type);
WTLC_LIB void WTLCAPI WTLC_Template_Destroy(_In_ WTLC_Template * toast);

WTLC_LIB void WTLCAPI WTLC_Template_setFirstLine(_In_ WTLC_Template * toast, _In_ LPCWSTR text);
WTLC_LIB void WTLCAPI WTLC_Template_setSecondLine(_In_ WTLC_Template * toast, _In_ LPCWSTR text);
WTLC_LIB void WTLCAPI WTLC_Template_setThirdLine(_In_ WTLC_Template * toast, _In_ LPCWSTR text);
WTLC_LIB void WTLCAPI WTLC_Template_setTextField(_In_ WTLC_Template * toast, _In_ LPCWSTR txt, _In_ WTLC_TextField pos);
WTLC_LIB void WTLCAPI WTLC_Template_setAttributionText(_In_ WTLC_Template * toast, _In_ LPCWSTR attributionText);
WTLC_LIB void WTLCAPI WTLC_Template_setImagePath(_In_ WTLC_Template * toast, _In_ LPCWSTR imgPath);
WTLC_LIB void WTLCAPI WTLC_Template_setImagePathWithCropHint(_In_ WTLC_Template * toast, _In_ LPCWSTR imgPath, _In_ WTLC_CropHint cropHint);
WTLC_LIB void WTLCAPI WTLC_Template_setHeroImagePath(_In_ WTLC_Template * toast, _In_ LPCWSTR imgPath, _In_ BOOL inlineImage);
WTLC_LIB void WTLCAPI WTLC_Template_setAudioSystemFile(_In_ WTLC_Template * toast, _In_ WTLC_AudioSystemFile audio);
WTLC_LIB void WTLCAPI WTLC_Template_setAudioPath(_In_ WTLC_Template * toast, _In_ LPCWSTR audioPath);
WTLC_LIB void WTLCAPI WTLC_Template_setAudioOption(_In_ WTLC_Template * toast, _In_ WTLC_AudioOption audioOption);
WTLC_LIB void WTLCAPI WTLC_Template_setDuration(_In_ WTLC_Template * toast, _In_ WTLC_Duration duration);
WTLC_LIB void WTLCAPI WTLC_Template_setExpiration(_In_ WTLC_Template * toast, _In_ INT64 millisecondsFromNow);
WTLC_LIB void WTLCAPI WTLC_Template_setScenario(_In_ WTLC_Template * toast, _In_ WTLC_Scenario scenario);
WTLC_LIB void WTLCAPI WTLC_Template_addAction(_In_ WTLC_Template * toast, _In_ LPCWSTR label);

WTLC_LIB size_t WTLCAPI WTLC_Template_textFieldsCount(_In_ WTLC_Template * toast);
WTLC_LIB size_t WTLCAPI WTLC_Template_actionsCount(_In_ WTLC_Template * toast);
WTLC_LIB BOOL WTLCAPI WTLC_Template_hasImage(_In_ WTLC_Template * toast);
WTLC_LIB BOOL WTLCAPI WTLC_Template_hasHeroImage(_In_ WTLC_Template * toast);
WTLC_LIB LPCWSTR WTLCAPI WTLC_Template_textField(_In_ WTLC_Template * toast, _In_ WTLC_TextField pos);
WTLC_LIB LPCWSTR WTLCAPI WTLC_Template_actionLabel(_In_ WTLC_Template * toast, _In_ size_t pos);
WTLC_LIB LPCWSTR WTLCAPI WTLC_Template_imagePath(_In_ WTLC_Template * toast);
WTLC_LIB LPCWSTR WTLCAPI WTLC_Template_heroImagePath(_In_ WTLC_Template * toast);
WTLC_LIB LPCWSTR WTLCAPI WTLC_Template_audioPath(_In_ WTLC_Template * toast);
WTLC_LIB LPCWSTR WTLCAPI WTLC_Template_attributionText(_In_ WTLC_Template * toast);
WTLC_LIB LPCWSTR WTLCAPI WTLC_Template_scenario(_In_ WTLC_Template * toast);
WTLC_LIB INT64 WTLCAPI WTLC_Template_expiration(_In_ WTLC_Template * toast);
WTLC_LIB WTLC_TemplateType WTLCAPI WTLC_Template_type(_In_ WTLC_Template * toast);
WTLC_LIB WTLC_AudioOption WTLCAPI WTLC_Template_audioOption(_In_ WTLC_Template * toast);
WTLC_LIB WTLC_Duration WTLCAPI WTLC_Template_duration(_In_ WTLC_Template * toast);
WTLC_LIB BOOL WTLCAPI WTLC_Template_isToastGeneric(_In_ WTLC_Template * toast);
WTLC_LIB BOOL WTLCAPI WTLC_Template_isInlineHeroImage(_In_ WTLC_Template * toast);
WTLC_LIB BOOL WTLCAPI WTLC_Template_isCropHintCircle(_In_ WTLC_Template * toast);

enum _WTLC_Error
{
    WTLC_Error_NoError = 0,
    WTLC_Error_NotInitialized,
    WTLC_Error_SystemNotSupported,
    WTLC_Error_ShellLinkNotCreated,
    WTLC_Error_InvalidAppUserModelID,
    WTLC_Error_InvalidParameters,
    WTLC_Error_InvalidHandler,
    WTLC_Error_NotDisplayed,
    WTLC_Error_UnknownError
};
typedef enum _WTLC_Error WTLC_Error;

enum _WTLC_ShortcutResult
{
    WTLC_SHORTCUT_UNCHANGED = 0,
    WTLC_SHORTCUT_WAS_CHANGED = 1,
    WTLC_SHORTCUT_WAS_CREATED = 2,
    WTLC_SHORTCUT_MISSING_PARAMETERS = -1,
    WTLC_SHORTCUT_INCOMPATIBLE_OS = -2,
    WTLC_SHORTCUT_COM_INIT_FAILURE = -3,
    WTLC_SHORTCUT_CREATE_FAILED = -4
};
typedef enum _WTLC_ShortcutResult WTLC_ShortcutResult;

enum _WTLC_ShortcutPolicy
{
    WTLC_SHORTCUT_POLICY_IGNORE = 0,
    WTLC_SHORTCUT_POLICY_REQUIRE_NO_CREATE = 1,
    WTLC_SHORTCUT_POLICY_REQUIRE_CREATE = 2
};
typedef enum _WTLC_ShortcutPolicy WTLC_ShortcutPolicy;

typedef struct _WTLC_Instance WTLC_Instance;

WTLC_LIB WTLC_Instance * WTLCAPI WTLC_Instance_Create(void);
WTLC_LIB void WTLCAPI WTLC_Instance_Destroy(_In_ WTLC_Instance * instance);

WTLC_LIB BOOL WTLCAPI WTLC_isCompatible(void);
WTLC_LIB BOOL WTLCAPI WTLC_isSupportingModernFeatures(void);
WTLC_LIB BOOL WTLCAPI WTLC_isWin10AnniversaryOrHigher(void);
WTLC_LIB LPCWSTR WTLCAPI WTLC_strerror(_In_ WTLC_Error error);
WTLC_LIB BOOL WTLCAPI WTLC_initialize(_In_ WTLC_Instance * instance, _Out_opt_ WTLC_Error * error);
WTLC_LIB BOOL WTLCAPI WTLC_isInitialized(_In_ WTLC_Instance * instance);
WTLC_LIB BOOL WTLCAPI WTLC_hideToast(_In_ WTLC_Instance * instance, _In_ INT64 id);
WTLC_LIB INT64 WTLCAPI WTLC_showToast(_In_ WTLC_Instance * instance,
                                      _In_ WTLC_Template * toast,
                                      _In_opt_ void * userData,
                                      _In_opt_ WTLC_CB_toastActivated toastActivated,
                                      _In_opt_ WTLC_CB_toastActivatedAction toastActivatedAction,
                                      _In_opt_ WTLC_CB_toastDismissed toastDismissed,
                                      _In_opt_ WTLC_CB_toastFailed toastFailed,
                                      _Out_opt_ WTLC_Error * error);
WTLC_LIB void WTLCAPI WTLC_clear(_In_ WTLC_Instance * instance);
WTLC_LIB WTLC_ShortcutResult WTLCAPI WTLC_createShortcut(_In_ WTLC_Instance * instance);

WTLC_LIB LPCWSTR WTLCAPI WTLC_appName(_In_ WTLC_Instance * instance);
WTLC_LIB LPCWSTR WTLCAPI WTLC_appUserModelId(_In_ WTLC_Instance * instance);
WTLC_LIB void WTLCAPI WTLC_setAppUserModelId(_In_ WTLC_Instance * instance, _In_ LPCWSTR aumi);
WTLC_LIB void WTLCAPI WTLC_setAppName(_In_ WTLC_Instance * instance, _In_ LPCWSTR appName);
WTLC_LIB void WTLCAPI WTLC_setShortcutPolicy(_In_ WTLC_Instance * instance, _In_ WTLC_ShortcutPolicy policy);

#if defined (__cplusplus)
}
#endif

#endif
