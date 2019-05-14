package com.candibell.sdk.rxbus

/**
 *
 * BaseEventType Enum name pattern: [SenderModuleName]_[ReceiverModuleName]_EVENT
 * SenderModuleName: the name of the module which will invoke Rxbus.instance.post(),
 * ReceiverModuleName: the name of the module which will invoke Rxbus.instance.register()
 * this field is optional if it is same to ReceiverModuleName
 */

enum class BaseEventType {
    UI_EVENT
}

open class BaseEvent(var baseEventType: BaseEventType)

/*
EventType Enum name pattern: [SenderModuleName]_[ReceiverModuleName]_[EventDescription]
SenderModuleName: the name of the module which will invoke Rxbus.instance.post(),
ReceiverModuleName: the name of the module which will invoke Rxbus.instance.register()
this field is optional if it is same to ReceiverModuleName
EventDescription: description of Rxbus event
 */

enum class SignInEventType {
    SIGH_IN_SUCCESS,
    SIGH_IN_FAIL,
    SIGN_IN_NEED_SIGN_UP,
    SIGN_IN_MFA
}

enum class SignUpEventType {
    SIGN_UP_SUCCESS,
    SIGN_UP_FAIL,
    SIGN_UP_USER_EXIST,
    USER_NOT_FOUND_NEED_SIGN_UP,
    SIGN_UP_NEED_VERIFY
}

enum class ConfirmEventType {
    CONFIRM_SUCCESS,
    CONFIRM_RESEND,
    CONFIRM_FAIL
}

enum class ForgetPasswordType {
    FORGET_PASSWORD_SUCCESS,
    FORGET_PASSWORD_RESEND,
    FORGET_PASSWORD_FAIL,
    FORGET_PASSWORD_NEED_VERIFY
}

enum class ChangePasswordType {
    CHANGE_PASSWORD_SUCCESS,
    CHANGE_PASSWORD_FAIL
}

enum class SetupHubType {
    SETUP_HUB_SUCCESS,
    SETUP_HUB_CONNECTED,
    SETUP_HUB_DISCONNECTED,
    SETUP_HUB_FAIL
}

enum class PushType {
    PUSH_DEFAULT
}


data class UISetupHubEvent(var setupHubType: SetupHubType, var parameter: Any?) :
    BaseEvent(BaseEventType.UI_EVENT)

data class UISignInEvent(
    var signInEventType: SignInEventType,
    var parameter: Any? = ""
) :
    BaseEvent(BaseEventType.UI_EVENT)

data class UISignUpEvent(
    var signUpEventType: SignUpEventType,
    var parameter: Any? = ""
) :
    BaseEvent(BaseEventType.UI_EVENT)

data class UIConfirmSignUpEvent(
    var confirmEventType: ConfirmEventType,
    var parameter: Any? = ""
) :
    BaseEvent(BaseEventType.UI_EVENT)

data class UIForgetPasswordEvent(
    var forgetPasswordType: ForgetPasswordType,
    var parameter: Any? = ""
) :
    BaseEvent(BaseEventType.UI_EVENT)

data class UIChangePasswordEvent(
    var changePasswordType: ChangePasswordType,
    var parameter: Any? = ""
) :
    BaseEvent(BaseEventType.UI_EVENT)

data class UIPushEvent(
    var pushType: PushType,
    var parameter: Any? = ""
) :
    BaseEvent(BaseEventType.UI_EVENT)