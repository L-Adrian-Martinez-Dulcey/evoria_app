package com.example.p3.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.p3.R
import com.example.p3.ui.viewmodel.UserViewModel
import com.example.p3.ui.theme.AppTheme

@Composable
fun LoginScreen(navController: NavController, userViewModel: UserViewModel) {
    // Estados para los campos
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Logic states from UserViewModel
    val loginResult by userViewModel.loginResult.collectAsState()
    val loginError by userViewModel.loginError.collectAsState()
    val isAuthLoading by userViewModel.isAuthLoading.collectAsState()

    // Paleta de colores EVORIA
    val navy = Color(0xFF2F4156)
    val teal = Color(0xFF567C8D)
    val beige = Color(0xFFF5EFE6)
    val textSecondary = Color(0xFF7A8F9E)
    val inputBg = Color(0xFFF2F5F7)

    // Handle login success
    if (loginResult != null) {
        LaunchedEffect(Unit) {
            navController.navigate("home") {
                popUpTo("login") { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFF4F7FA),
                        Color(0xFFE8EDF0)
                    ),
                    radius = 1200f,
                    center = Offset(300f, 200f)
                )
            )
    ) {
        // Formas decorativas abstractas (muy sutiles)
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = 120.dp, y = (-80).dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFCD9E6B).copy(alpha = 0.04f),
                            Color.Transparent
                        ),
                        radius = 300f
                    )
                )
        )

        Box(
            modifier = Modifier
                .size(220.dp)
                .offset(x = (-60).dp, y = 500.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            navy.copy(alpha = 0.03f),
                            Color.Transparent
                        ),
                        radius = 250f
                    )
                )
        )

        // Contenido principal
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // ===== SECCIÓN SUPERIOR: TÍTULOS + LOGO =====
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Badge "GESTIÓN DE EVENTOS"
                Surface(
                    modifier = Modifier
                        .shadow(
                            elevation = 3.dp,
                            shape = RoundedCornerShape(40.dp),
                            clip = false
                        ),
                    shape = RoundedCornerShape(40.dp),
                    color = beige,
                ) {
                    Text(
                        text = "GESTIÓN DE EVENTOS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.2.sp,
                        color = navy,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Título "Bienvenido a EVORIA"
                Text(
                    text = "Bienvenido a EVORIA",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.4).sp,
                    color = navy,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Eslogan
                Text(
                    text = "— Donde la planificación se convierte en resultados —",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp),
                    fontStyle = FontStyle.Italic,
                    letterSpacing = 0.3.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Logo de Evoria (Redondeado y con estilo)
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .shadow(
                            elevation = 8.dp,
                            shape = CircleShape,
                            clip = false
                        )
                        .clip(CircleShape)
                        .background(Color.White)
                        .padding(4.dp), // Margen interno para el logo
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logoevoria),
                        contentDescription = "Logo de Evoria",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Subtítulo
                Text(
                    text = "Ingresa para continuar",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Normal,
                    color = textSecondary,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ===== FORMULARIO =====
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Campo: Correo electrónico
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(
                            text = "Correo electrónico",
                            color = navy,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    placeholder = {
                        Text(
                            text = "usuario@evoria.com",
                            color = Color(0xFFA0B4C2)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Email",
                            tint = textSecondary
                        )
                    },
                    shape = RoundedCornerShape(18.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = teal,
                        unfocusedBorderColor = Color.Transparent,
                        focusedLabelColor = navy,
                        unfocusedLabelColor = navy,
                        focusedLeadingIconColor = teal,
                        unfocusedLeadingIconColor = textSecondary,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = inputBg,
                        focusedTextColor = navy,
                        unfocusedTextColor = navy
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF1E2C38)
                    )
                )

                // Campo: Contraseña
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(
                            text = "Contraseña",
                            color = navy,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    placeholder = {
                        Text(
                            text = "••••••••",
                            color = Color(0xFFA0B4C2)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Contraseña",
                            tint = textSecondary
                        )
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = { passwordVisible = !passwordVisible }
                        ) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña",
                                tint = textSecondary
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    shape = RoundedCornerShape(18.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = teal,
                        unfocusedBorderColor = Color.Transparent,
                        focusedLabelColor = navy,
                        unfocusedLabelColor = navy,
                        focusedLeadingIconColor = teal,
                        unfocusedLeadingIconColor = textSecondary,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = inputBg,
                        focusedTextColor = navy,
                        unfocusedTextColor = navy
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF1E2C38)
                    )
                )

                // Error message
                if (loginError != null) {
                    Text(
                        text = loginError ?: "",
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Botón: Iniciar sesión
                Button(
                    onClick = {
                        userViewModel.loginUser(email, password)
                    },
                    enabled = !isAuthLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(62.dp)
                        .shadow(
                            elevation = 10.dp,
                            shape = RoundedCornerShape(30.dp),
                            clip = false
                        ),
                    shape = RoundedCornerShape(30.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = navy,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 6.dp
                    )
                ) {
                    if (isAuthLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Iniciar sesión",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Iniciar sesión",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.4.sp,
                            color = Color.White
                        )
                    }
                }
            }

            // Espacio inferior (solo para equilibrio visual)
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    AppTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFF4F7FA)
        ) {
            // Placeholder content for preview if needed
        }
    }
}
