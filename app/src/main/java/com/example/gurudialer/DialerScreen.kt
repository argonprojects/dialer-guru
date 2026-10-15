package com.example.gurudialer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

// Classic Warm Ivory & Vintage Brass Palette for high contrast and low eye strain
private val ClassicCreamBg = Color(0xFFFDF8EE)
private val ClassicHeaderBrown = Color(0xFF3E2723)
private val ClassicGoldBorder = Color(0xFFC5A059)
private val CallYesGreen = Color(0xFF1B873B)
private val CancelNoRed = Color(0xFFC62828)

@Composable
fun DialerScreen(
    contacts: List<GuruContact>,
    isDefaultDialer: Boolean,
    onRequestDefaultDialer: () -> Unit,
    onContactSelected: (GuruContact) -> Unit,
    onConfirmCall: (GuruContact) -> Unit,
    onCancelSelection: () -> Unit
) {
    var activeContact by remember { mutableStateOf<GuruContact?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ClassicCreamBg)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Classic Header Bar
            Surface(
                color = ClassicHeaderBrown,
                shadowElevation = 6.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Dialer Guru",
                        color = Color(0xFFFFF8E7),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif
                    )

                    if (!isDefaultDialer) {
                        Button(
                            onClick = onRequestDefaultDialer,
                            colors = ButtonDefaults.buttonColors(containerColor = ClassicGoldBorder)
                        ) {
                            Text(
                                text = "Set Default Phone",
                                color = ClassicHeaderBrown,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }

            if (contacts.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No contacts found in your phone book yet.",
                        fontSize = 24.sp,
                        color = ClassicHeaderBrown,
                        textAlign = TextAlign.Center,
                        fontFamily = FontFamily.Serif
                    )
                }
            } else {
                // 2-Column Photo Grid with generous spacing
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(contacts, key = { it.id }) { contact ->
                        ClassicPhotoCard(
                            contact = contact,
                            onTap = {
                                onContactSelected(contact)
                                activeContact = contact
                            }
                        )
                    }
                }
            }
        }

        // Full-Page Yes / No Confirmation Screen with smooth spring expansion
        AnimatedVisibility(
            visible = activeContact != null,
            enter = fadeIn() + scaleIn(
                initialScale = 0.85f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ),
            exit = fadeOut() + scaleOut(targetScale = 0.85f)
        ) {
            activeContact?.let { selected ->
                FullScreenConfirmCallOverlay(
                    contact = selected,
                    onConfirmYes = {
                        val target = selected
                        activeContact = null
                        onConfirmCall(target)
                    },
                    onCancelNo = {
                        activeContact = null
                        onCancelSelection()
                    }
                )
            }
        }
    }
}

@Composable
fun ClassicPhotoCard(
    contact: GuruContact,
    onTap: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "card_press_scale"
    )

    Card(
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(3.dp, ClassicGoldBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .height(245.dp)
            .scale(scale)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = { onTap() }
                )
            }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Contact Portrait Photo or High-Contrast Avatar
            if (contact.photoUri != null) {
                AsyncImage(
                    model = contact.photoUri,
                    contentDescription = contact.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFE6D7C3)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = contact.name,
                        tint = ClassicHeaderBrown,
                        modifier = Modifier.size(96.dp)
                    )
                }
            }

            // Star badge for favorite contacts
            if (contact.isStarred) {
                Surface(
                    shape = CircleShape,
                    color = ClassicHeaderBrown.copy(alpha = 0.85f),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Favorite",
                        tint = Color(0xFFFFD54F),
                        modifier = Modifier.padding(6.dp)
                    )
                }
            }

            // Bottom gradient name banner with oversized serif label
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.82f)
                            )
                        )
                    )
                    .padding(horizontal = 10.dp, vertical = 12.dp)
            ) {
                Text(
                    text = contact.name,
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Full-page confirmation screen preventing accidental dials.
 * Displays the contact photo full-screen with unmistakable YES (Green Call) and NO (Red Cancel) buttons.
 */
@Composable
fun FullScreenConfirmCallOverlay(
    contact: GuruContact,
    onConfirmYes: () -> Unit,
    onCancelNo: () -> Unit
) {
    Surface(
        color = ClassicHeaderBrown,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Contact Name Banner
            Text(
                text = "Call ${contact.name}?",
                color = Color(0xFFFFF8E7),
                fontSize = 34.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Serif,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 12.dp)
            )

            // Large Framed Full-Page Portrait
            Card(
                shape = RoundedCornerShape(32.dp),
                border = BorderStroke(5.dp, ClassicGoldBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 16.dp)
            ) {
                if (contact.photoUri != null) {
                    AsyncImage(
                        model = contact.photoUri,
                        contentDescription = contact.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFE6D7C3)),
                            contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = contact.name,
                            tint = ClassicHeaderBrown,
                            modifier = Modifier.size(160.dp)
                        )
                    }
                }
            }

            // Huge Side-by-Side NO (Red) and YES (Green) Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // NO / CANCEL BUTTON
                Button(
                    onClick = onCancelNo,
                    colors = ButtonDefaults.buttonColors(containerColor = CancelNoRed),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(110.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "No, Cancel",
                            tint = Color.White,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "NO",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }

                // YES / CALL BUTTON
                Button(
                    onClick = onConfirmYes,
                    colors = ButtonDefaults.buttonColors(containerColor = CallYesGreen),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(110.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Yes, Call",
                            tint = Color.White,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "YES",
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
