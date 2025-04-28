package io.github.kdroidfilter.knotify.platform.windows.constants

internal object WTLC_TextField_Constants {
    const val FirstLine = 0
    const val SecondLine = 1
    const val ThirdLine = 2
}

internal object WTLC_TemplateType_Constants {
    const val ImageAndText02 = 1
    const val Text02 = 2
}

internal object WTLC_AudioOption_Constants {
    const val Default = 0
}

internal object WTLC_DismissalReason_Constants {
    const val UserCanceled = 0
    const val ApplicationHidden = 1
    const val TimedOut = 2
}

internal object WTLC_ShortcutPolicy_Constants {
    const val IGNORE = 0
}

internal const val PM_REMOVE = 0x0001
internal const val QS_KEY = 0x0001
internal const val QS_MOUSEMOVE = 0x0002
internal const val QS_MOUSEBUTTON = 0x0004
internal const val QS_POSTMESSAGE = 0x0008
internal const val QS_TIMER = 0x0010
internal const val QS_PAINT = 0x0020
internal const val QS_SENDMESSAGE = 0x0040
internal const val QS_HOTKEY = 0x0080
internal const val QS_ALLPOSTMESSAGE = 0x0100
internal const val QS_RAWINPUT = 0x0400
internal const val QS_MOUSE = QS_MOUSEMOVE or QS_MOUSEBUTTON
internal const val QS_INPUT = QS_MOUSE or QS_KEY or QS_RAWINPUT
internal const val QS_ALLEVENTS = QS_INPUT or QS_POSTMESSAGE or QS_TIMER or QS_PAINT or QS_HOTKEY
internal const val QS_ALLINPUT = QS_INPUT or QS_POSTMESSAGE or QS_TIMER or QS_PAINT or QS_HOTKEY or QS_SENDMESSAGE