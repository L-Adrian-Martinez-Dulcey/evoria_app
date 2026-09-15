package com.example.p3.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.p3.R
import com.example.p3.ui.viewmodel.UserViewModel

@Composable
fun RegisterScreen(
    navController: NavHostController,
    users: UserViewModel
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }

    var localError by remember { mutableStateOf<String?>(null) }
    var isRegistering by remember { mutableStateOf(false) }

    val error by users.error.collectAsState()

    LaunchedEffect(error) {
        if (error != null) {
            isRegistering = false
        }
    }

    val navy = Color(0xFF2F4156)
    val teal = Color(0xFF567C8D)
    val beige = Color(0xFFF5EFE6)
    val textSecondary = Color(0xFF7A8F9E)
    val inputBg = Color(0xFFF2F5F7)
    val fieldText = Color(0xFF1E2C38)

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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 12.dp)
            ) {
                Surface(
                    modifier = Modifier.shadow(
                        elevation = 3.dp,
                        shape = RoundedCornerShape(40.dp),
                        clip = false
                    ),
                    shape = RoundedCornerShape(40.dp),
                    color = beige
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

                Text(
                    text = "Únete a EVORIA",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.4).sp,
                    color = navy,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "— Crea experiencias que conectan —",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp),
                    fontStyle = FontStyle.Italic,
                    letterSpacing = 0.3.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

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
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logoevoria),
                        contentDescription = "Logo de Evoria",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Regístrate para comenzar a usar EVORIA",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Normal,
                    color = textSecondary,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                RegisterTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        localError = null
                    },
                    label = "Nombre",
                    placeholder = "Tu nombre completo",
                    icon = { Icon(Icons.Default.Person, contentDescription = "Nombre") },
                    navy = navy,
                    teal = teal,
                    textSecondary = textSecondary,
                    inputBg = inputBg,
                    fieldText = fieldText
                )

                RegisterTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        localError = null
                    },
                    label = "Correo electrónico",
                    placeholder = "usuario@evoria.com",
                    icon = { Icon(Icons.Default.Email, contentDescription = "Email") },
                    keyboardType = KeyboardType.Email,
                    navy = navy,
                    teal = teal,
                    textSecondary = textSecondary,
                    inputBg = inputBg,
                    fieldText = fieldText
                )

                RegisterTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        localError = null
                    },
                    label = "Contraseña",
                    placeholder = "••••••••",
                    icon = { Icon(Icons.Default.Lock, contentDescription = "Contraseña") },
                    keyboardType = KeyboardType.Password,
                    visualTransformation = PasswordVisualTransformation(),
                    navy = navy,
                    teal = teal,
                    textSecondary = textSecondary,
                    inputBg = inputBg,
                    fieldText = fieldText
                )

                RegisterTextField(
                    value = phone,
                    onValueChange = {
                        phone = it
                        localError = null
                    },
                    label = "Teléfono",
                    placeholder = "Tu número de teléfono",
                    icon = { Icon(Icons.Default.Phone, contentDescription = "Teléfono") },
                    keyboardType = KeyboardType.Phone,
                    navy = navy,
                    teal = teal,
                    textSecondary = textSecondary,
                    inputBg = inputBg,
                    fieldText = fieldText
                )

                RegisterTextField(
                    value = city,
                    onValueChange = {
                        city = it
                        localError = null
                    },
                    label = "Ciudad",
                    placeholder = "Tu ciudad",
                    icon = { Icon(Icons.Default.LocationOn, contentDescription = "Ciudad") },
                    navy = navy,
                    teal = teal,
                    textSecondary = textSecondary,
                    inputBg = inputBg,
                    fieldText = fieldText
                )

                localError?.let {
                    Text(
                        text = it,
                        color = Color(0xFFB3261E),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                error?.let {
                    Text(
                        text = it,
                        color = Color(0xFFB3261E),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = {
                        when {
                            name.isBlank() ||
                                    email.isBlank() ||
                                    password.isBlank() ||
                                    phone.isBlank() ||
                                    city.isBlank() -> {
                                localError = "Completa todos los campos"
                            }

                            !android.util.Patterns.EMAIL_ADDRESS
                                .matcher(email)
                                .matches() -> {
                                localError = "Ingresa un correo válido"
                            }

                            password.length < 6 -> {
                                localError = "La contraseña debe tener al menos 6 caracteres"
                            }

                            else -> {
                                localError = null
                                isRegistering = true

                                users.registerUser(
                                    name = name.trim(),
                                    email = email.trim(),
                                    password = password,
                                    phone = phone.trim(),
                                    city = city.trim()
                                ) {
                                    navController.navigate("login") {
                                        popUpTo("register") {
                                            inclusive = true
                                        }
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(62.dp)
                        .shadow(
                            elevation = 10.dp,
                            shape = RoundedCornerShape(30.dp),
                            clip = false
                        ),
                    enabled = !isRegistering,
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
                    if (isRegistering) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Crear cuenta",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Crear cuenta",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.4.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = {
                    navController.navigate("login") {
                        popUpTo("register") {
                            inclusive = true
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "¿Ya tienes una cuenta? Inicia sesión",
                    color = navy,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun RegisterTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    icon: @Composable () -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation = androidx.compose.ui.text.input.VisualTransformation.None,
    navy: Color,
    teal: Color,
    textSecondary: Color,
    inputBg: Color,
    fieldText: Color
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = {
            Text(
                text = label,
                color = navy,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        },
        placeholder = {
            Text(
                text = placeholder,
                color = Color(0xFFA0B4C2)
            )
        },
        leadingIcon = {
            icon()
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
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = visualTransformation,
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            fontWeight = FontWeight.Normal,
            color = fieldText
        )
    )
}
