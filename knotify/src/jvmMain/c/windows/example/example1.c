/**
 * This example is licensed under a Creative Commons Zero v1.0 Universal License
 * https://creativecommons.org/publicdomain/zero/1.0/legalcode
 */

#include <Windows.h>

#include "wintoastlibc.h"

int main(int argc, char ** argv)
{
    INT ret = EXIT_SUCCESS;
    WTLC_Instance * instance = NULL;
    WTLC_Template * templ = NULL;
    WTLC_Error error = WTLC_Error_NoError;

    if(!WTLC_isCompatible())
    {
        MessageBoxW(NULL, L"Your system is not compatible!", L"Error", MB_OK | MB_ICONERROR);
        return EXIT_FAILURE;
    }

    CoInitializeEx(NULL, COINIT_APARTMENTTHREADED);
    instance = WTLC_Instance_Create();
    if(!instance)
    {
        MessageBoxW(NULL, L"WinToast instance creation failed!", L"Error", MB_OK | MB_ICONERROR);
        ret = EXIT_FAILURE;
        goto cleanup;
    }

    WTLC_setAppName(instance, L"Example");
    WTLC_setAppUserModelId(instance, L"Microsoft.Windows.Explorer");
    WTLC_setShortcutPolicy(instance, WTLC_SHORTCUT_POLICY_IGNORE);
    if(!WTLC_initialize(instance, &error))
    {
        MessageBoxW(NULL, WTLC_strerror(error), L"Error", MB_OK | MB_ICONERROR);
        ret = EXIT_FAILURE;
        goto cleanup;
    }

    templ = WTLC_Template_Create(WTLC_TemplateType_Text01);
    WTLC_Template_setFirstLine(templ, L"HELLO, WORLD!");
    if(WTLC_showToast(instance, templ, NULL, NULL, NULL, NULL, NULL, &error) < 0)
    {
        MessageBoxW(NULL, WTLC_strerror(error), L"Error", MB_OK | MB_ICONERROR);
        ret = EXIT_FAILURE;
        goto cleanup;
    }

    Sleep(1000);

cleanup:
    WTLC_Template_Destroy(templ);
    WTLC_Instance_Destroy(instance);
    CoUninitialize();
    return ret;
}

INT WINAPI wWinMain(HINSTANCE hInstance, HINSTANCE hPrevInstance, PWSTR lpCmdLine, INT nCmdShow)
{
    return main(0, NULL);
}
