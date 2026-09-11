package com.example.p3.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

private val Navy = Color(0xFF2F4156)
private val Teal = Color(0xFF567C8D)
private val Beige = Color(0xFFF5EFE6)

data class OnboardingPage(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val title: String,
    val description: String
)

@Composable
fun OnboardingScreen(
    onRegister: () -> Unit,
    onLogin: () -> Unit
) {
    val pages = listOf(

        OnboardingPage(
            icon = Icons.Default.Event,
            title = "Bienvenido a EVORIA",
            description = "Una plataforma diseñada para centralizar la planificación y gestión de tus eventos en un solo lugar."
        ),

        OnboardingPage(
            icon = Icons.Default.Inventory2,
            title = "Todo bajo control",
            description = "Gestiona eventos, recursos, inventario, presupuestos y cronogramas de forma organizada y eficiente."
        ),

        OnboardingPage(
            icon = Icons.Default.TrendingUp,
            title = "Planifica. Organiza. Logra.",
            description = "Esperamos que EVORIA haga que la planificación de tus eventos sea más sencilla, rápida y confiable."
        )
    )

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { pages.size }
    )

    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Navy, Teal)
                )
            )
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = 75.dp,
                    start = 24.dp,
                    end = 24.dp,
                    bottom = 30.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "EVORIA",
                color = Color.White,
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(25.dp))

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->

                OnboardingPageContent(
                    page = pages[page]
                )
            }

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {

                pages.indices.forEach { index ->

                    Box(
                        modifier = Modifier
                            .padding(horizontal = 5.dp)
                            .size(
                                width = if (pagerState.currentPage == index) 25.dp else 9.dp,
                                height = 9.dp
                            )
                            .clip(CircleShape)
                            .background(
                                if (pagerState.currentPage == index)
                                    Beige
                                else
                                    Color.White.copy(alpha = 0.35f)
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(25.dp))

            Button(
                onClick = {

                    if (pagerState.currentPage == pages.lastIndex) {
                        onRegister()
                    } else {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(
                                pagerState.currentPage + 1
                            )
                        }
                    }

                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Beige,
                    contentColor = Navy
                )
            ) {

                Text(
                    text = if (pagerState.currentPage == pages.lastIndex)
                        "Registrarse"
                    else
                        "Siguiente",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (pagerState.currentPage == 0) {

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = "¿Ya tienes una cuenta? ",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 14.sp
                    )

                    Text(
                        text = "Inicia sesión",
                        color = Beige,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable {
                            onLogin()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun OnboardingPageContent(
    page: OnboardingPage
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 15.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Box(
            modifier = Modifier
                .size(130.dp)
                .clip(CircleShape)
                .background(
                    Color.White.copy(alpha = 0.12f)
                ),
            contentAlignment = Alignment.Center
        ) {

            Icon(
                imageVector = page.icon,
                contentDescription = null,
                tint = Beige,
                modifier = Modifier.size(70.dp)
            )
        }

        Spacer(modifier = Modifier.height(35.dp))

        Text(
            text = page.title,
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = page.description,
            color = Color.White.copy(alpha = 0.82f),
            fontSize = 17.sp,
            lineHeight = 27.sp,
            textAlign = TextAlign.Center
        )
    }
}