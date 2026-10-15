package com.example.gurudialer

import android.os.Bundle
import android.telecom.Call
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Simplified, high-contrast In-Call screen for incoming and ongoing calls.
 */
class InCallActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val callState by CallManager.callState.collectAsState()
            val callerNumber by CallManager.callerNumber.collectAsState()
            val isSpeakerOn by CallManager.isSpeakerOn.collectAsState()

            LaunchedEffect(callState) {
                if (callState == Call.STATE_DISCONNECTED) {
                    finish()
                }
            }

            Surface(
                color = Color(0xFF261815),
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 36.dp)
                    ) {
                        Text(
                            text = when (callState) {
                                Call.STATE_RINGING -> "Incoming Call..."
                                Call.STATE_DIALING, Call.STATE_CONNECTING -> "Calling..."
                                Call.STATE_ACTIVE -> "Connected"
                                else -> "Ending Call..."
                            },
                            color = Color(0xFFFFD54F),
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = callerNumber,
                            color = Color.White,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center
                        )
                    }

                    // Giant Speakerphone Toggle Button
                    Button(
                        onClick = { CallManager.toggleSpeakerphone() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSpeakerOn) Color(0xFFFFB300) else Color(0xFF4E342E)
                        ),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(96.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Speakerphone",
                            tint = if (isSpeakerOn) Color.Black else Color.White,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (isSpeakerOn) "SPEAKER ON" else "TURN ON SPEAKER",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isSpeakerOn) Color.Black else Color.White
                        )
                    }

                    // Bottom Action Buttons: Answer + Hang Up (when ringing) or Giant Hang Up
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        if (callState == Call.STATE_RINGING) {
                            Button(
                                onClick = { CallManager.answerCall() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B873B)),
                                shape = RoundedCornerShape(24.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(115.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.Call,
                                        contentDescription = "Answer",
                                        tint = Color.White,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Text(
                                        text = "ANSWER",
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = { CallManager.hangUpCall() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(115.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.CallEnd,
                                    contentDescription = "Hang Up",
                                    tint = Color.White,
                                    modifier = Modifier.size(48.dp)
                                )
                                Text(
                                    text = "HANG UP",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
