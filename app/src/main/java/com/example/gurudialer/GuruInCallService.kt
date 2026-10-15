package com.example.gurudialer

import android.content.Intent
import android.telecom.Call
import android.telecom.CallAudioState
import android.telecom.InCallService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Holds active telecom call state and binds Android's Telecom subsystem to InCallActivity.
 */
object CallManager {
    private var currentCall: Call? = null
    var inCallService: GuruInCallService? = null

    private val _callState = MutableStateFlow(Call.STATE_DISCONNECTED)
    val callState: StateFlow<Int> = _callState

    private val _callerNumber = MutableStateFlow("")
    val callerNumber: StateFlow<String> = _callerNumber

    private val _isSpeakerOn = MutableStateFlow(false)
    val isSpeakerOn: StateFlow<Boolean> = _isSpeakerOn

    private val callback = object : Call.Callback() {
        override fun onStateChanged(call: Call, state: Int) {
            _callState.value = state
            if (state == Call.STATE_DISCONNECTED) {
                currentCall?.unregisterCallback(this)
                currentCall = null
            }
        }
    }

    fun attachCall(call: Call) {
        currentCall?.unregisterCallback(callback)
        currentCall = call
        call.registerCallback(callback)
        _callState.value = call.state
        _callerNumber.value = call.details?.handle?.schemeSpecificPart ?: "Unknown Caller"
    }

    fun answerCall() {
        currentCall?.answer(0)
    }

    fun hangUpCall() {
        currentCall?.disconnect()
    }

    fun toggleSpeakerphone() {
        val nextState = !_isSpeakerOn.value
        _isSpeakerOn.value = nextState
        val route = if (nextState) {
            CallAudioState.ROUTE_SPEAKER
        } else {
            CallAudioState.ROUTE_EARPIECE
        }
        inCallService?.setAudioRoute(route)
    }
}

class GuruInCallService : InCallService() {

    override fun onCreate() {
        super.onCreate()
        CallManager.inCallService = this
    }

    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        CallManager.attachCall(call)
        val intent = Intent(this, InCallActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        startActivity(intent)
    }

    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
        CallManager.hangUpCall()
    }

    override fun onDestroy() {
        CallManager.inCallService = null
        super.onDestroy()
    }
}
