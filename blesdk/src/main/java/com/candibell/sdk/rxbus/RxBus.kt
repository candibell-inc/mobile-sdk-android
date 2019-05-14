package com.candibell.sdk.rxbus

import com.candibell.sdk.d
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.annotations.Nullable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

class RxBus {

    private val mBus = PublishSubject.create<Any>().toSerialized()

    companion object {
        val instance: RxBus =
            RxBus()
    }

    fun post(event: Any) {
        d("post: ${event.javaClass.simpleName}")
        mBus.onNext(event)
    }

    fun <T> register(eventType: Class<T>): Observable<T> {
        d("register: ${eventType.javaClass.simpleName}")
        return mBus.ofType(eventType).observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
    }

    fun postRxEvent(eventType: Any) {
        postRxEvent(eventType, null)
    }

    fun postRxEvent(eventType: Any, @Nullable eventData: Any?) {
        when (eventType) {
            is SignInEventType -> {
                d("post: " + eventType.name)
                post(UISignInEvent(eventType, eventData))
            }
            is SignUpEventType -> {
                d("post: " + eventType.name)
                post(UISignUpEvent(eventType, eventData))
            }
            is ConfirmEventType -> {
                d("post: " + eventType.name)
                post(UIConfirmSignUpEvent(eventType, eventData))
            }
            is ForgetPasswordType -> {
                d("post: " + eventType.name)
                post(UIForgetPasswordEvent(eventType, eventData))
            }
            is SetupHubType -> {
                d("post: " + eventType.name)
                post(UISetupHubEvent(eventType, eventData))
            }
            is ChangePasswordType -> {
                d("post: " + eventType.name)
                post(UIChangePasswordEvent(eventType, eventData))
            }
            is PushType -> {
                d("post: " + eventType.name)
                post(UIPushEvent(eventType, eventData))
            }
        }
    }
}