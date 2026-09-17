@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.p3.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfDocument
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.p3.data.model.Event
import com.example.p3.data.model.User
import com.example.p3.data.model.AppNotification
import com.example.p3.data.image.fastImageUrl
import com.example.p3.ui.viewmodel.EventViewModel
import com.example.p3.ui.viewmodel.UserViewModel
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun EventHomeScreen(viewModel: EventViewModel, userViewModel: UserViewModel, navController: NavController) {
    val state by viewModel.uiState.collectAsState()
    val isDarkMode by userViewModel.isDarkMode.collectAsState()
    val currentUser by userViewModel.currentUser.collectAsState()
    val feedbackHost = remember { SnackbarHostState() }
    val upcomingEvents = state.events
        .filter { !it.hasEnded() }
        .sortedWith(compareBy<Event> { it.date }.thenBy { it.time })
    val featuredEvent = upcomingEvents.firstOrNull()
    val popularEvents = upcomingEvents
        .sortedWith(
            compareByDescending<Event> { it.registrations.size }
                .thenByDescending { event ->
                    event.reviews.map { it.rating }.average().takeIf { !it.isNaN() } ?: 0.0
                }
        )
        .take(5)
    EventFeedback(state.error, state.message, feedbackHost) { viewModel.clearMessage() }
    Scaffold(
        snackbarHost = { SnackbarHost(feedbackHost) },
    ) { padding ->
        if (state.isLoading && state.events.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceContainerLowest),
                contentPadding = PaddingValues(start = 16.dp, top = 18.dp, end = 16.dp, bottom = 92.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column {
                            Text(
                                "EVORIA",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp,
                                color = Color(0xFF244B68),
                            )
                            Text(
                                "Hola, ${currentUser?.name?.substringBefore(" ") ?: "organizador"}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Row {
                            IconButton(onClick = { userViewModel.toggleDarkMode() }) {
                                Icon(
                                    if (isDarkMode) Icons.Default.WbSunny else Icons.Default.DarkMode,
                                    contentDescription = "Cambiar modo de tema",
                                )
                            }
                            IconButton(onClick = { navController.navigate("notifications") }) {
                                Icon(Icons.Default.NotificationsNone, "Notificaciones")
                            }
                        }
                    }
                }

                item {
                    Text("Explora categorías", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        listOf("Música", "Tecnología", "Gastronomía", "Arte", "Deporte").forEach { category ->
                            AssistChip(
                                onClick = { navController.navigate("event_search") },
                                label = { Text(category) },
                                leadingIcon = { Icon(Icons.Default.Category, null, Modifier.size(18.dp)) },
                                shape = RoundedCornerShape(14.dp),
                            )
                        }
                    }
                }

                item {
                    Text("Evento destacado", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(10.dp))
                    if (featuredEvent == null) {
                        EmptyHomeCard("Aún no hay eventos próximos para mostrar.")
                    } else {
                        FeaturedEventCard(featuredEvent) {
                            featuredEvent.id?.let { navController.navigate("event_detail/${Uri.encode(it)}") }
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Eventos populares", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (popularEvents.isEmpty()) {
                            EmptyHomeCard("Cuando existan eventos, aparecerán aquí.")
                        } else {
                            popularEvents.forEach { event ->
                                HomeEventRow(event) {
                                    event.id?.let { navController.navigate("event_detail/${Uri.encode(it)}") }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FeaturedEventCard(event: Event, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF183A59)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp),
        ) {
            if (event.coverImage.isNotBlank()) {
                AsyncImage(
                    model = event.coverImage.fastImageUrl(),
                    contentDescription = event.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    alpha = 0.62f,
                )
            }
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color(0xFF102B43).copy(alpha = 0.55f))
                    .padding(18.dp),
                contentAlignment = Alignment.BottomStart,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(
                        "RECOMENDADO PARA TI",
                        color = Color(0xFFB9D9E5),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                    )
                    Text(
                        event.title,
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        "${event.date} · ${event.place}",
                        color = Color.White.copy(alpha = 0.88f),
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeEventRow(event: Event, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (event.coverImage.isNotBlank()) {
                AsyncImage(
                    model = event.coverImage.fastImageUrl(),
                    contentDescription = event.title,
                    modifier = Modifier
                        .size(width = 92.dp, height = 78.dp)
                        .clip(RoundedCornerShape(14.dp)),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(width = 92.dp, height = 78.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFDDEAF0)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Event, null, tint = Color(0xFF406370))
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    event.title,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    "${event.date} · ${event.time}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    event.place,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF567C8D),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun EmptyHomeCard(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Text(
            message,
            modifier = Modifier.padding(18.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun EventCard(event: Event, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (event.coverImage.isNotBlank()) {
                AsyncImage(
                    model = event.coverImage.fastImageUrl(),
                    contentDescription = null,
                    modifier = Modifier
                        .size(130.dp) // even bigger
                        .clip(MaterialTheme.shapes.medium),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(16.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.headlineMedium.copy(fontSize = 28.sp),
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = event.description,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
fun EventDetailScreen(
    eventId: String,
    user: User,
    viewModel: EventViewModel,
    users: List<User>,
    navController: NavController,
) {
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(eventId) {
        viewModel.loadEvent(eventId)
    }
    val event = state.events.firstOrNull { it.id == eventId }
    var confirmDelete by remember { mutableStateOf(false) }
    var confirmUnregister by remember { mutableStateOf(false) }
    var showReview by remember { mutableStateOf(false) }
    if (event == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (state.error != null) Text(state.error!!)
            else CircularProgressIndicator()
        }
        return
    }
    val isCreator = event.creatorId == user.id
    val isRegistered = event.registrations.any { it.userId == user.id }
    val creator = users.firstOrNull { it.id == event.creatorId }
    val creatorName = creator?.name
        ?: if (event.creatorId == user.id) user.name else "Organizador de EVORIA"
    val averageRating = event.reviews
        .map { it.rating }
        .takeIf { it.isNotEmpty() }
        ?.average()
    var qrBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var qrError by remember { mutableStateOf<String?>(null) }
    val detailSnackbarHostState = remember { SnackbarHostState() }
    val snackbarScope = rememberCoroutineScope()
    val context = LocalContext.current
    val pdfLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/pdf"),
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.openOutputStream(uri)?.use { output ->
                    createEventPdf(event, users).writeTo(output)
                } ?: error("No fue posible abrir el archivo.")
            }.onSuccess {
                snackbarScope.launch { detailSnackbarHostState.showSnackbar("PDF generado correctamente") }
            }.onFailure {
                snackbarScope.launch {
                    detailSnackbarHostState.showSnackbar("No fue posible generar el PDF: ${it.message}")
                }
            }
        }
    }

    LaunchedEffect(state.message, state.error) {
        state.message?.let { detailSnackbarHostState.showSnackbar(it) }
        state.error?.let { detailSnackbarHostState.showSnackbar(it) }
        if (state.message != null || state.error != null) {
            viewModel.clearMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(detailSnackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Detalle del evento") },
                navigationIcon = {
                    IconButton({ navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        LazyColumn(
            Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                if (event.coverImage.isNotBlank()) {
                    AsyncImage(
                        model = event.coverImage.fastImageUrl(),
                        contentDescription = "Imagen de ${event.title}",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .clip(RoundedCornerShape(26.dp)),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp),
                        shape = RoundedCornerShape(26.dp),
                        color = Color(0xFFF5EFE6),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Event,
                                contentDescription = "Sin imagen de evento",
                                modifier = Modifier.size(72.dp),
                                tint = Color(0xFF567C8D),
                            )
                        }
                    }
                }
            }

            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(26.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            event.title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2F4156),
                        )
                        EventInfoLine(
                            Icons.Default.CalendarMonth,
                            "${event.date} · ${event.time.ifBlank { "Hora no definida" }}",
                        )
                        EventInfoLine(
                            Icons.Default.LocationOn,
                            event.place.ifBlank { "Lugar por confirmar" },
                        )
                        if (event.category.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color(0xFFDDEAF0),
                            ) {
                                Text(
                                    event.category,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color(0xFF406370),
                                )
                            }
                        }
                        averageRating?.let { rating ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("★", color = Color(0xFFB7832F), fontSize = 20.sp)
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    String.format(Locale.US, "%.1f", rating),
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2F4156),
                                )
                                Text(
                                    " · ${event.reviews.size} reseña${if (event.reviews.size == 1) "" else "s"}",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }

            item {
                EventDetailSection(
                    title = "Organizador",
                    icon = Icons.Default.Person,
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                event.creatorId.takeIf { it.isNotBlank() }?.let {
                                    navController.navigate("public_profile/${Uri.encode(it)}")
                                }
                            },
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            if (!creator?.avatar.isNullOrBlank()) {
                                AsyncImage(
                                    model = creator?.avatar?.fastImageUrl(),
                                    contentDescription = "Foto de $creatorName",
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop,
                                )
                            } else {
                                Surface(
                                    modifier = Modifier.size(54.dp),
                                    shape = CircleShape,
                                    color = Color(0xFFDDEAF0),
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            creatorName.take(1).uppercase(),
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF406370),
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(creatorName, fontWeight = FontWeight.Bold)
                                Text(
                                    creator?.email ?: "Ver información del organizador",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = "Ver perfil del organizador",
                                tint = Color(0xFF567C8D),
                            )
                        }
                    }
                }
            }

            item {
                EventDetailSection(
                    title = "Información del evento",
                    icon = Icons.Default.Info,
                ) {
                    Text(
                        event.description,
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = 24.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            if (isCreator) {
                item {
                    EventDetailSection(
                        title = "Asistentes (${event.registrations.size})",
                        icon = Icons.Default.Group,
                    ) {
                        if (event.registrations.isEmpty()) {
                            Text(
                                "Todavía no hay usuarios inscritos.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                event.registrations.forEach { registration ->
                                    val attendee = users.firstOrNull { it.id == registration.userId }
                                    val attendeeName = attendee?.name?.trim().orEmpty()
                                    ListItem(
                                        leadingContent = {
                                            if (!attendee?.avatar.isNullOrBlank()) {
                                                AsyncImage(
                                                    model = attendee?.avatar,
                                                    contentDescription = "Foto de $attendeeName",
                                                    modifier = Modifier
                                                        .size(40.dp)
                                                        .clip(CircleShape),
                                                    contentScale = ContentScale.Crop,
                                                )
                                            } else {
                                                Surface(
                                                    modifier = Modifier.size(40.dp),
                                                    shape = CircleShape,
                                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Text(
                                                            text = attendeeName
                                                                .trim()
                                                                .take(1)
                                                                .uppercase()
                                                                .ifBlank { "U" },
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                        )
                                                    }
                                                }
                                            }
                                        },
                                        headlineContent = {
                                            Text(attendee?.name ?: "Usuario no disponible")
                                        },
                                        supportingContent = {
                                            Text(attendee?.email ?: "Inscripción registrada")
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (event.hasEnded() && event.reviews.isNotEmpty()) {
                item {
                    EventDetailSection(title = "Reseñas", icon = Icons.Default.Star) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            event.reviews.forEach { review ->
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                                ) {
                                    Column(Modifier.padding(12.dp)) {
                                        Text(
                                            "★ ${review.rating}/5",
                                            color = Color(0xFFB7832F),
                                            fontWeight = FontWeight.SemiBold,
                                        )
                                        if (review.comment.isNotBlank()) {
                                            Text(review.comment, style = MaterialTheme.typography.bodyMedium)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                if (isCreator) {
                    Button(
                        onClick = {
                            pdfLauncher.launch(
                                "EVORIA_${event.title.replace(Regex("[^A-Za-z0-9_-]"), "_")}.pdf"
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF244B68)),
                    ) {
                        Icon(Icons.Default.PictureAsPdf, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Generar PDF del evento")
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            { navController.navigate("event_form/${event.id}") },
                            modifier = Modifier.weight(1f),
                        ) {
                            Icon(Icons.Default.Edit, null)
                            Spacer(Modifier.width(6.dp))
                            Text("Editar")
                        }
                        OutlinedButton(
                            { confirmDelete = true },
                            modifier = Modifier.weight(1f),
                        ) {
                            Icon(Icons.Default.Delete, null)
                            Spacer(Modifier.width(6.dp))
                            Text("Eliminar")
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = {
                                if (isRegistered) confirmUnregister = true
                                else viewModel.register(event, user.id.orEmpty(), user.name)
                            },
                            enabled = !state.isLoading && (isRegistered || event.availableSlots > 0),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isRegistered) Color(0xFF567C8D) else Color(0xFF2F4156),
                            ),
                        ) {
                            if (state.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp,
                                )
                            } else {
                                Icon(
                                    if (isRegistered) Icons.Default.PersonRemove else Icons.Default.PersonAdd,
                                    null,
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(if (isRegistered) "Desinscribirme" else "Inscribirme")
                            }
                        }
                        if (isRegistered && event.hasEnded()) {
                            OutlinedButton(
                                { showReview = true },
                                enabled = !state.isLoading,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Icon(Icons.Default.Star, null)
                                Spacer(Modifier.width(8.dp))
                                Text("Calificar evento")
                            }
                        }
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            if (event.place.isBlank()) {
                                qrError = "El evento no tiene una ubicación disponible."
                            } else {
                                qrError = null
                                qrBitmap = generateQrCode(
                                    "https://www.google.com/maps/search/?api=1&query=${
                                        URLEncoder.encode(event.place, StandardCharsets.UTF_8.toString())
                                    }"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF567C8D)),
                    ) {
                        Icon(Icons.Default.QrCode, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Generar QR de Ubicación")
                    }
                    qrError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                    qrBitmap?.let { bitmap ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            color = MaterialTheme.colorScheme.surface,
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(16.dp),
                            ) {
                                Text("QR de ubicación", style = MaterialTheme.typography.titleSmall)
                                Image(
                                    bitmap = bitmap,
                                    contentDescription = "Código QR de ubicación",
                                    modifier = Modifier.size(200.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    if (confirmDelete) AlertDialog(onDismissRequest = { confirmDelete = false }, title = { Text("¿Eliminar evento?") }, text = { Text("Esta acción no se puede deshacer.") }, confirmButton = { TextButton({ viewModel.delete(event, user.id.orEmpty()) { navController.popBackStack() } }) { Text("Eliminar") } }, dismissButton = { TextButton({ confirmDelete = false }) { Text("Cancelar") } })
    if (confirmUnregister) {
        AlertDialog(
            onDismissRequest = { confirmUnregister = false },
            title = { Text("¿Quieres cancelar tu inscripción?") },
            text = { Text("Dejarás de estar inscrito en este evento.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmUnregister = false
                        viewModel.unregister(event, user.id.orEmpty())
                    },
                    enabled = !state.isLoading,
                ) {
                    Text("Desinscribirme", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton({ confirmUnregister = false }) { Text("Cancelar") }
            },
        )
    }
    if (showReview) ReviewDialog(onDismiss = { showReview = false }) { rating, comment -> viewModel.addReview(event, user.id.orEmpty(), rating, comment); showReview = false }
}

@Composable private fun DetailLine(label: String, value: String) { Text("$label: $value") }

@Composable
private fun EventInfoLine(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(22.dp),
            tint = Color(0xFF567C8D),
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun EventInfoGrid(event: Event) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            EventInfoItem(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.CalendarMonth,
                label = "Fecha",
                value = event.date,
            )
            EventInfoItem(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Schedule,
                label = "Hora",
                value = event.time.ifBlank { "No definida" },
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            EventInfoItem(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.LocationOn,
                label = "Ubicación",
                value = event.place,
            )
            EventInfoItem(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Category,
                label = "Categoría",
                value = event.category,
            )
        }
        EventInfoItem(
            modifier = Modifier.fillMaxWidth(),
            icon = Icons.Default.People,
            label = "Cupos disponibles",
            value = event.availableSlots.toString(),
        )
    }
}

@Composable
fun PublicProfileScreen(profile: User?, navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil del organizador") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                },
            )
        },
    ) { padding ->
        if (profile == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                Text("No fue posible encontrar este perfil.")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                if (!profile.avatar.isNullOrBlank()) {
                    AsyncImage(
                        model = profile.avatar.fastImageUrl(),
                        contentDescription = "Foto de ${profile.name}",
                        modifier = Modifier
                            .size(104.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Surface(
                        modifier = Modifier.size(104.dp),
                        shape = CircleShape,
                        color = Color(0xFFDDEAF0),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                profile.name.take(1).uppercase().ifBlank { "U" },
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF406370),
                            )
                        }
                    }
                }
                Text(
                    profile.name.ifBlank { "Organizador de EVORIA" },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF244B68),
                    textAlign = TextAlign.Center,
                )
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        if (profile.email.isNotBlank()) {
                            EventInfoLine(Icons.Default.Email, profile.email)
                        }
                        if (profile.phone.isNotBlank()) {
                            EventInfoLine(Icons.Default.Phone, profile.phone)
                        }
                        if (profile.city.isNotBlank()) {
                            EventInfoLine(Icons.Default.LocationOn, profile.city)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EventInfoItem(
    modifier: Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, tint = Color(0xFF567C8D))
            Spacer(Modifier.width(8.dp))
            Column {
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    value,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun EventDetailSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = Color(0xFF567C8D))
                Spacer(Modifier.width(8.dp))
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF2F4156),
                )
            }
            content()
        }
    }
}

@Composable
fun EventFormScreen(eventId: String?, user: User, viewModel: EventViewModel, navController: NavController) {
    val state by viewModel.uiState.collectAsState(); val existing = state.events.firstOrNull { it.id == eventId }
    val canEdit = existing == null || existing.creatorId == user.id
    val context = LocalContext.current
    LaunchedEffect(eventId) {
        if (eventId != null && existing == null) viewModel.loadEvent(eventId)
    }
    var title by remember(existing?.id) { mutableStateOf(existing?.title.orEmpty()) }; var description by remember(existing?.id) { mutableStateOf(existing?.description.orEmpty()) }
    var date by remember(existing?.id) { mutableStateOf(existing?.date.orEmpty()) }; var time by remember(existing?.id) { mutableStateOf(existing?.time.orEmpty()) }
    var place by remember(existing?.id) { mutableStateOf(existing?.place.orEmpty()) }; var category by remember(existing?.id) { mutableStateOf(existing?.category.orEmpty()) }
    var slots by remember(existing?.id) { mutableStateOf(existing?.availableSlots?.toString() ?: "") }; var image by remember(existing?.id) { mutableStateOf(existing?.coverImage.orEmpty()) }
    var selectedImageUri by remember(existing?.id) { mutableStateOf<Uri?>(null) }
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> uri?.let { selected ->
        runCatching { context.contentResolver.takePersistableUriPermission(selected, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
        selectedImageUri = selected
        image = selected.toString()
    } }
    if (eventId != null && existing == null && state.error == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }
    if (eventId != null && existing == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(state.error ?: "No fue posible cargar el evento.")
        }
        return
    }
    if (eventId != null && existing != null && !canEdit) {
        LaunchedEffect(eventId) { navController.popBackStack() }
        return
    }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            if (existing == null) "Crear evento" else "Editar evento",
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            if (existing == null) "Comparte una nueva experiencia" else "Actualiza los detalles de tu evento",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                navigationIcon = {
                    IconButton({ navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            state.error?.let {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                ) {
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(14.dp),
                    )
                }
            }

            EventFormCard(
                title = "Información principal",
                subtitle = "Dale identidad a tu evento",
            ) {
                EventFormField(
                    value = title,
                    onValueChange = { title = it },
                    label = "Título",
                    placeholder = "Nombre del evento",
                )
                EventFormField(
                    value = description,
                    onValueChange = { description = it },
                    label = "Descripción",
                    placeholder = "Describe la actividad y lo que podrán disfrutar",
                    singleLine = false,
                )
            }

            EventFormCard(
                title = "Cuándo y dónde",
                subtitle = "Ayuda a los asistentes a planificar",
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    EventDateButton(
                        value = date,
                        onClick = {
                            val c = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
                            DatePickerDialog(
                                context,
                                { _, y, m, d -> date = String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d) },
                                c.get(Calendar.YEAR),
                                c.get(Calendar.MONTH),
                                c.get(Calendar.DAY_OF_MONTH)
                            ).apply { datePicker.minDate = c.timeInMillis }.show()
                        },
                        modifier = Modifier.weight(1f),
                    )
                    EventTimeButton(
                        value = time,
                        onClick = {
                            val c = Calendar.getInstance()
                            TimePickerDialog(
                                context,
                                { _, h, m -> time = String.format(Locale.US, "%02d:%02d", h, m) },
                                c.get(Calendar.HOUR_OF_DAY),
                                c.get(Calendar.MINUTE),
                                true
                            ).show()
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
                EventFormField(
                    value = place,
                    onValueChange = { place = it },
                    label = "Lugar",
                    placeholder = "Dirección o punto de encuentro",
                )
            }

            EventFormCard(
                title = "Detalles del evento",
                subtitle = "Organiza la experiencia para tu comunidad",
            ) {
                EventCategoryField(value = category, onValueChange = { category = it })
                EventFormField(
                    value = slots,
                    onValueChange = { slots = it },
                    label = "Cupos disponibles",
                    placeholder = "Número de asistentes",
                    keyboard = KeyboardType.Number,
                )
            }

            EventFormCard(
                title = "Imagen de portada",
                subtitle = "Haz que tu evento destaque",
            ) {
                OutlinedButton(
                    onClick = { imagePicker.launch(arrayOf("image/*")) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF2F4156)
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.Brush.linearGradient(
                            listOf(Color(0xFF567C8D), Color(0xFF2F4156))
                        )
                    ),
                ) {
                    Icon(
                        if (image.isBlank()) Icons.Default.AddPhotoAlternate else Icons.Default.Edit,
                        null,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(if (image.isBlank()) "Seleccionar imagen" else "Cambiar imagen")
                }
                if (image.isNotBlank()) {
                    AsyncImage(
                        model = image.fastImageUrl(),
                        contentDescription = "Imagen del evento",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(190.dp)
                            .clip(RoundedCornerShape(18.dp)),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                            shape = RoundedCornerShape(18.dp),
                            color = Color(0xFFF5EFE6),
                    ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                            ) {
                                Text(
                                    "Puedes añadir una imagen de portada",
                                    color = Color(0xFF7A8F9E),
                                    textAlign = TextAlign.Center,
                                )
                        }
                    }
                }
            }

            Button(
                onClick = {
                    viewModel.save(
                        Event(existing?.id, existing?.creatorId ?: user.id.orEmpty(), title.trim(), description.trim(), date.trim(), time.trim(), place.trim(), category.trim(), slots.toIntOrNull() ?: -1, image.takeUnless { selectedImageUri != null }.orEmpty(), existing?.createdAt ?: now(), existing?.registrations ?: emptyList(), existing?.reviews ?: emptyList()),
                        user.id.orEmpty(),
                        selectedImageUri,
                    ) { navController.popBackStack() }
                },
                enabled = !state.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2F4156),
                    contentColor = Color.White,
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 8.dp,
                ),
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.White,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Icon(Icons.Default.Save, null)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (existing == null) "Crear evento" else "Guardar cambios",
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

@Composable
private fun EventFormCard(
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceContainerLow,
                RoundedCornerShape(20.dp)
            )
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF2F4156),
        )
        Text(
            subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF7A8F9E),
        )
        content()
    }
}

@Composable
private fun EventFormField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    keyboard: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        placeholder = { Text(placeholder, color = Color(0xFFA0B4C2)) },
        singleLine = singleLine,
        minLines = if (singleLine) 1 else 4,
        keyboardOptions = KeyboardOptions(keyboardType = keyboard),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF567C8D),
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            focusedLabelColor = Color(0xFF2F4156),
            focusedLeadingIconColor = Color(0xFF567C8D),
            unfocusedLeadingIconColor = Color(0xFF7A8F9E),
        ),
    )
}

@Composable
private fun EventDateButton(
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = Color(0xFF2F4156),
        ),
    ) {
        Text(
            if (value.isBlank()) "Fecha" else value,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun EventTimeButton(
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = Color(0xFF2F4156),
        ),
    ) {
        Text(
            if (value.isBlank()) "Hora" else value,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun EventCategoryField(
    value: String,
    onValueChange: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf("Arte", "Deporte", "Tecnología", "Música", "Gastronomía", "Educación", "Otra")
    val isOther = value.isNotBlank() && !options.contains(value)
    var showOtherField by remember { mutableStateOf(isOther) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        OutlinedTextField(
            value = if (showOtherField) "Otra" else value,
            onValueChange = {},
            readOnly = true,
            label = { Text("Categoría") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF567C8D),
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedLabelColor = Color(0xFF2F4156),
                focusedLeadingIconColor = Color(0xFF567C8D),
            ),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        if (option == "Otra") {
                            showOtherField = true
                            onValueChange("")
                        } else {
                            showOtherField = false
                            onValueChange(option)
                        }
                        expanded = false
                    },
                )
            }
        }
    }
    if (showOtherField) {
        EventFormField(
            value = value,
            onValueChange = onValueChange,
            label = "Escribe la categoría personalizada",
            placeholder = "Categoría",
        )
    }
}

@Composable private fun AppField(value: String, change: (String) -> Unit, label: String, keyboard: KeyboardType = KeyboardType.Text, single: Boolean = true, readOnly: Boolean = false) {
    OutlinedTextField(
        value,
        change,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = single,
        readOnly = readOnly,
        keyboardOptions = KeyboardOptions(keyboardType = keyboard),
    )
}

@Composable private fun DateField(value: String, onValue: (String) -> Unit) { val context = LocalContext.current; OutlinedButton(onClick = { val c = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }; DatePickerDialog(context, { _, y, m, d -> onValue(String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d)) }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).apply { datePicker.minDate = c.timeInMillis }.show() }, modifier = Modifier.fillMaxWidth()) { Text(if (value.isBlank()) "Seleccionar fecha" else "Fecha: $value") } }
@Composable private fun TimeField(value: String, onValue: (String) -> Unit) { val context = LocalContext.current; OutlinedButton(onClick = { val c = Calendar.getInstance(); TimePickerDialog(context, { _, h, m -> onValue(String.format(Locale.US, "%02d:%02d", h, m)) }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show() }, modifier = Modifier.fillMaxWidth()) { Text(if (value.isBlank()) "Seleccionar hora" else "Hora: $value") } }
@Composable private fun CategoryField(value: String, onValue: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf("Arte", "Deporte", "Tecnología", "Música", "Gastronomía", "Educación", "Otra")
    val isOther = value.isNotBlank() && !options.contains(value)
    var showOtherField by remember { mutableStateOf(isOther) }

    ExposedDropdownMenuBox(expanded, { expanded = it }) {
        OutlinedTextField(
            value = if (showOtherField) "Otra" else value,
            onValueChange = {},
            readOnly = true,
            label = { Text("Categoría") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded, { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        if (option == "Otra") {
                            showOtherField = true
                            onValue("")
                        } else {
                            showOtherField = false
                            onValue(option)
                        }
                        expanded = false
                    }
                )
            }
        }
    }
    if (showOtherField) {
        Spacer(modifier = Modifier.height(8.dp))
        AppField(value, onValue, "Escribe la categoría personalizada")
    }
}

@Composable
fun MyEventsScreen(user: User, viewModel: EventViewModel, navController: NavController) {
    val state by viewModel.uiState.collectAsState()
    var tab by remember { mutableStateOf(0) }
    val createdEvents = state.events.filter { it.creatorId == user.id }
    val registeredEvents = state.events.filter { event ->
        event.registrations.any { it.userId == user.id }
    }
    val selectedEvents = if (tab == 0) createdEvents else registeredEvents

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainerLowest),
        contentPadding = PaddingValues(start = 8.dp, top = 20.dp, end = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                "Mis eventos",
                modifier = Modifier.padding(horizontal = 2.dp),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF244B68),
            )
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                        RoundedCornerShape(18.dp),
                    )
                    .padding(4.dp),
            ) {
                listOf("Creados", "Participando").forEachIndexed { index, label ->
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { tab = index },
                        shape = RoundedCornerShape(15.dp),
                        color = if (tab == index) Color(0xFF244B68) else Color.Transparent,
                    ) {
                        Text(
                            label,
                            modifier = Modifier.padding(vertical = 13.dp),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.SemiBold,
                            color = if (tab == index) Color.White else Color(0xFF244B68),
                        )
                    }
                }
            }
        }

        if (state.isLoading && state.events.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = Color(0xFF244B68))
                }
            }
        } else if (selectedEvents.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Icon(
                            if (tab == 0) Icons.Default.EventAvailable else Icons.Default.EventBusy,
                            contentDescription = null,
                            modifier = Modifier.size(42.dp),
                            tint = Color(0xFF567C8D),
                        )
                        Text(
                            if (tab == 0) "Todavía no has creado eventos"
                            else "Todavía no tienes inscripciones",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            if (tab == 0) {
                                "Crea tu primer evento desde el botón central."
                            } else {
                                "Explora eventos y reserva tu lugar en los que te interesen."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        } else {
            itemsIndexed(
                selectedEvents,
                key = { index, event -> event.id ?: "my-event-$index" },
            ) { _, event ->
                MyEventCard(event, tab == 0) {
                    event.id?.let { navController.navigate("event_detail/${Uri.encode(it)}") }
                }
            }
        }
    }
}

@Composable
private fun MyEventCard(
    event: Event,
    isCreated: Boolean,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (event.coverImage.isNotBlank()) {
                AsyncImage(
                    model = event.coverImage.fastImageUrl(),
                    contentDescription = event.title,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(14.dp)),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFDDEAF0)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Event, null, tint = Color(0xFF406370))
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Text(
                    event.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF244B68),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFF567C8D),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "${event.date} · ${event.time}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFF567C8D),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        event.place.ifBlank { "Lugar por confirmar" },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (event.hasEnded()) {
                        MaterialTheme.colorScheme.surfaceVariant
                    } else {
                        Color(0xFFDDF2E9)
                    },
                ) {
                    Text(
                        when {
                            event.hasEnded() -> "Finalizado"
                            isCreated -> "Publicado"
                            else -> "Participando"
                        },
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (event.hasEnded()) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            Color(0xFF27705D)
                        },
                    )
                }
            }
            Icon(
                Icons.Default.MoreVert,
                contentDescription = "Ver opciones",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun ProfileScreen(user: User, userViewModel: UserViewModel, navController: NavController) {
    val context = LocalContext.current
    val eventViewModel: EventViewModel = viewModel()
    val eventState by eventViewModel.uiState.collectAsState()
    val profileError by userViewModel.profileError.collectAsState()
    val isLoading by userViewModel.isLoading.collectAsState()
    var dataExpanded by remember(user.id) { mutableStateOf(false) }
    var upcomingExpanded by remember(user.id) { mutableStateOf(false) }
    var popularExpanded by remember(user.id) { mutableStateOf(false) }
    var editingData by remember(user.id) { mutableStateOf(false) }
    var name by remember(user.id) { mutableStateOf(user.name) }
    var email by remember(user.id) { mutableStateOf(user.email) }
    var phone by remember(user.id) { mutableStateOf(user.phone) }
    var city by remember(user.id) { mutableStateOf(user.city) }
    var selectedImageUri by remember(user.id) { mutableStateOf<Uri?>(null) }
    var selectedImagePreview by remember(user.id) { mutableStateOf<String?>(null) }
    var showLogoutConfirmation by remember { mutableStateOf(false) }
    var showSaveConfirmation by remember { mutableStateOf(false) }
    var showPhotoConfirmation by remember { mutableStateOf(false) }
    var showSavedMessage by remember { mutableStateOf(false) }
    var showPhotoSavedMessage by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> uri?.let { selected ->
        runCatching { context.contentResolver.takePersistableUriPermission(selected, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
        selectedImageUri = selected
        selectedImagePreview = selected.toString()
        dataExpanded = true
        showPhotoConfirmation = true
    } }

    LaunchedEffect(showSavedMessage, showPhotoSavedMessage) {
        if (showSavedMessage) {
            snackbarHostState.showSnackbar("¡Datos actualizados!\nTu información se guardó correctamente.")
            showSavedMessage = false
        }
        if (showPhotoSavedMessage) {
            snackbarHostState.showSnackbar("¡Foto actualizada!\nTu foto de perfil se cambió correctamente.")
            showPhotoSavedMessage = false
        }
    }

    val upcomingEvents = eventState.events
        .filter { event ->
            event.registrations.any { it.userId == user.id } &&
                !event.hasEnded() &&
                isWithinNextSevenDays(event.date, event.time)
        }
        .sortedWith(compareBy<Event> { it.date }.thenBy { it.time })
    val popularEvents = eventState.events
        .filter { event ->
            event.creatorId == user.id &&
                event.reviews.isNotEmpty() &&
                event.reviews.map { it.rating }.average() in 4.5..5.0
        }
        .sortedByDescending { event -> event.reviews.map { it.rating }.average() }
    val createdEvents = eventState.events.filter { it.creatorId == user.id }
    val attendedEvents = eventState.events.count { event ->
        event.registrations.any { it.userId == user.id }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Mi perfil", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton({ navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { dataExpanded = !dataExpanded }) {
                        Icon(Icons.Default.Settings, "Configuración del perfil")
                    }
                },
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                ProfileHeader(
                    user = user,
                    avatar = selectedImagePreview ?: user.avatar.orEmpty(),
                    isLoading = isLoading,
                    onChangePhoto = { imagePicker.launch(arrayOf("image/jpeg", "image/png")) }
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            RoundedCornerShape(20.dp),
                        )
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    ProfileMetric(createdEvents.size.toString(), "Eventos creados")
                    ProfileMetric(attendedEvents.toString(), "Eventos asistidos")
                    ProfileMetric(eventState.events.sumOf { it.reviews.count { review -> review.userId == user.id } }.toString(), "Reseñas")
                }
            }

            item {
                Button(
                    onClick = {
                        dataExpanded = true
                        editingData = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F6B87)),
                ) {
                    Icon(Icons.Default.Edit, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Editar perfil", fontWeight = FontWeight.SemiBold)
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        "Mis eventos",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF244B68),
                    )
                    TextButton(onClick = { navController.navigate("my_events") }) {
                        Text("Ver todos", color = Color(0xFF567C8D))
                        Icon(Icons.Default.ChevronRight, null, Modifier.size(18.dp))
                    }
                }
            }

            if (createdEvents.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                    ) {
                        Text(
                            "Aún no has creado eventos.",
                            modifier = Modifier.padding(18.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else {
                items(
                    createdEvents.take(3),
                    key = { event -> event.id ?: event.title },
                ) { event ->
                    UpcomingEventCard(event) {
                        event.id?.let { navController.navigate("event_detail/${Uri.encode(it)}") }
                    }
                }
            }

            item {
                ProfileSectionCard(
                    title = "Mis datos",
                    subtitle = "Información personal y de contacto",
                    icon = Icons.Default.Person,
                    expanded = dataExpanded,
                    onClick = { dataExpanded = !dataExpanded }
                ) {
                    AnimatedVisibility(
                        visible = dataExpanded,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            ProfileField(name, { name = it }, "Nombre", Icons.Default.Person, readOnly = !editingData)
                            ProfileField(email, { email = it }, "Correo electrónico", Icons.Default.Email, KeyboardType.Email, readOnly = !editingData)
                            ProfileField(phone, { phone = it }, "Teléfono", Icons.Default.Phone, KeyboardType.Phone, readOnly = !editingData)
                            ProfileField(city, { city = it }, "Ciudad", Icons.Default.LocationOn, readOnly = !editingData)

                            profileError?.let {
                                Text(
                                    text = it,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )
                            }

                            Button(
                                onClick = {
                                    if (editingData) showSaveConfirmation = true
                                    else editingData = true
                                },
                                enabled = !isLoading,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF2F4156)
                                )
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(if (editingData) Icons.Default.Save else Icons.Default.Edit, null)
                                    Spacer(Modifier.width(8.dp))
                                    Text(if (editingData) "Guardar cambios" else "Actualizar datos")
                                }
                            }
                        }
                    }
                }
            }

            item {
                OutlinedButton(
                    onClick = { showLogoutConfirmation = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF2F4156)
                    )
                ) {
                    Icon(Icons.Default.Logout, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Cerrar sesión", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }

    if (showSaveConfirmation) {
        AlertDialog(
            onDismissRequest = { showSaveConfirmation = false },
            title = { Text("¿Guardar cambios?") },
            text = { Text("¿Deseas guardar los cambios realizados en tus datos?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSaveConfirmation = false
                        userViewModel.updateProfile(
                            user.copy(
                                name = name,
                                email = email,
                                phone = phone,
                                city = city,
                                avatar = user.avatar,
                            )
                        ) {
                            editingData = false
                            showSavedMessage = true
                        }
                    }
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveConfirmation = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showPhotoConfirmation) {
        AlertDialog(
            onDismissRequest = {
                showPhotoConfirmation = false
                selectedImageUri = null
                selectedImagePreview = null
            },
            title = { Text("¿Cambiar foto de perfil?") },
            text = { Text("La nueva foto reemplazará la foto actual.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPhotoConfirmation = false
                        selectedImageUri?.let { imageUri ->
                            userViewModel.updateProfile(
                                user.copy(
                                    name = user.name,
                                    email = user.email,
                                    phone = user.phone,
                                    city = user.city,
                                    avatar = user.avatar,
                                ),
                                imageUri,
                            ) {
                                selectedImageUri = null
                                selectedImagePreview = null
                                showPhotoSavedMessage = true
                            }
                        }
                    },
                    enabled = !isLoading
                ) {
                    Text("Cambiar foto")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showPhotoConfirmation = false
                        selectedImageUri = null
                        selectedImagePreview = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showLogoutConfirmation) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmation = false },
            title = { Text("¿Cerrar sesión?") },
            text = { Text("Tu sesión se cerrará en este dispositivo.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutConfirmation = false
                        userViewModel.logout {
                            navController.navigate("onboarding") {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    }
                ) {
                    Text("Cerrar sesión", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirmation = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun ProfileHeader(
    user: User,
    avatar: String,
    isLoading: Boolean,
    onChangePhoto: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(142.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            Surface(
                modifier = Modifier
                    .size(132.dp)
                    .align(Alignment.Center)
                    .border(2.dp, Color(0xFF567C8D), CircleShape)
                    .padding(4.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                if (avatar.isNotBlank()) {
                    AsyncImage(
                        model = avatar.fastImageUrl(),
                        contentDescription = "Foto de perfil",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Sin foto de perfil",
                            modifier = Modifier.size(58.dp),
                            tint = Color(0xFF567C8D)
                        )
                    }
                }
            }

            SmallFloatingActionButton(
                onClick = { if (!isLoading) onChangePhoto() },
                modifier = Modifier.size(40.dp),
                containerColor = Color(0xFF2F4156),
                contentColor = Color.White
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(17.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.Edit, "Cambiar foto", modifier = Modifier.size(18.dp))
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        Text(
            text = user.name,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2F4156)
        )
    }
}

@Composable
private fun ProfileSectionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    expanded: Boolean,
    onClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFF5EFE6),
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = null, tint = Color(0xFF567C8D))
                    }
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2F4156)
                    )
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Contraer" else "Expandir",
                    tint = Color(0xFF567C8D)
                )
            }
            content()
        }
    }
}

@Composable
private fun ProfileMetric(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF244B68),
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ProfileField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    readOnly: Boolean = false,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null) },
        singleLine = true,
        readOnly = readOnly,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF567C8D),
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            focusedLabelColor = Color(0xFF2F4156),
            focusedLeadingIconColor = Color(0xFF567C8D)
        )
    )
}

@Composable
private fun UpcomingEventCard(event: Event, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (event.coverImage.isNotBlank()) {
                AsyncImage(
                    model = event.coverImage.fastImageUrl(),
                    contentDescription = "Imagen de ${event.title}",
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Surface(
                    modifier = Modifier.size(64.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF5EFE6)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Event, null, tint = Color(0xFF567C8D))
                    }
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    event.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    event.date + if (event.time.isBlank()) "" else " · ${event.time}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(Icons.Default.ChevronRight, null, tint = Color(0xFF567C8D))
        }
    }
}

private fun isWithinNextSevenDays(date: String, time: String): Boolean = runCatching {
    val format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).apply { isLenient = false }
    val eventDate = format.parse("$date $time") ?: return false
    val today = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val limit = (today.clone() as Calendar).apply {
        add(Calendar.DAY_OF_YEAR, 7)
    }
    !eventDate.before(today.time) && !eventDate.after(limit.time)
}.getOrDefault(false)

@Composable
private fun EventFeedback(
    error: String?,
    message: String?,
    host: SnackbarHostState,
    clear: () -> Unit,
) {
    LaunchedEffect(error, message) {
        val text = error ?: message
        if (text != null) {
            host.showSnackbar(text)
            clear()
        }
    }
}
@Composable private fun ReviewDialog(onDismiss: () -> Unit, save: (Int, String) -> Unit) { var rating by remember { mutableStateOf("") }; var comment by remember { mutableStateOf("") }; AlertDialog(onDismissRequest = onDismiss, title = { Text("Calificar evento") }, text = { Column { AppField(rating, { rating = it }, "Puntaje (1-5)", KeyboardType.Number); AppField(comment, { comment = it }, "Comentario", single = false) } }, confirmButton = { TextButton({ save(rating.toIntOrNull() ?: 0, comment) }) { Text("Publicar") } }, dismissButton = { TextButton(onDismiss) { Text("Cancelar") } }) }
private fun now() = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(Calendar.getInstance().time)

private fun createEventPdf(event: Event, users: List<User>): PdfDocument {
    val document = PdfDocument()
    val pageWidth = 595
    val pageHeight = 842
    val margin = 42f
    val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(36, 75, 104)
        textSize = 22f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    val sectionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(36, 75, 104)
        textSize = 14f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.DKGRAY
        textSize = 11f
    }
    val logoPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(86, 124, 141)
        textSize = 18f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    var pageNumber = 1
    var page = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
    var canvas = page.canvas
    var y = 48f

    fun nextPage() {
        document.finishPage(page)
        pageNumber += 1
        page = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
        canvas = page.canvas
        y = 48f
        canvas.drawText("EVORIA", margin, y, logoPaint)
        y += 32f
    }

    fun line(text: String, paint: Paint = bodyPaint, spacing: Float = 18f) {
        if (y > pageHeight - 55) nextPage()
        canvas.drawText(text.take(92), margin, y, paint)
        y += spacing
    }

    canvas.drawText("EVORIA", margin, y, logoPaint)
    y += 42f
    line("Reporte del evento", sectionPaint, 26f)
    line(event.title, titlePaint, 32f)
    line("Fecha: ${event.date} · Hora: ${event.time.ifBlank { "No definida" }}")
    line("Lugar: ${event.place.ifBlank { "No definido" }}")
    line("Categoría: ${event.category.ifBlank { "Sin categoría" }}")
    line("Cupos disponibles: ${event.availableSlots}")
    y += 8f
    line("Descripción", sectionPaint, 22f)
    event.description.ifBlank { "Sin descripción." }
        .chunked(92)
        .forEach { line(it, bodyPaint, 16f) }
    y += 10f
    line("Asistentes inscritos (${event.registrations.size})", sectionPaint, 24f)
    if (event.registrations.isEmpty()) {
        line("No hay asistentes inscritos.", bodyPaint)
    } else {
        event.registrations.forEachIndexed { index, registration ->
            if (y > pageHeight - 70) nextPage()
            val attendee = users.firstOrNull { it.id == registration.userId }
            val name = attendee?.name?.ifBlank { "Usuario ${index + 1}" } ?: "Usuario ${index + 1}"
            val email = attendee?.email?.ifBlank { "Correo no disponible" } ?: "Correo no disponible"
            line("${index + 1}. $name", bodyPaint, 16f)
            line("   $email · Inscrito: ${registration.registrationDate.take(10)}", bodyPaint, 16f)
            y += 4f
        }
    }
    canvas.drawText("Documento generado por EVORIA", margin, pageHeight - 28f, bodyPaint)
    document.finishPage(page)
    return document
}

private fun generateQrCode(text: String): ImageBitmap? {
    return try {
        val matrix = QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, 512, 512)
        val bitmap = Bitmap.createBitmap(matrix.width, matrix.height, Bitmap.Config.RGB_565)
        for (x in 0 until matrix.width) {
            for (y in 0 until matrix.height) {
                bitmap.setPixel(x, y, if (matrix.get(x, y)) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
        bitmap.asImageBitmap()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

@Composable
fun EventSearchScreen(viewModel: EventViewModel, navController: NavController) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf("") }
    val categories = listOf("Arte", "Deporte", "Tecnología", "Música", "Gastronomía", "Educación")
    val filteredEvents = state.events.filter { event ->
        val matchesName = event.title.contains(searchQuery, ignoreCase = true) ||
            event.description.contains(searchQuery, ignoreCase = true) ||
            event.place.contains(searchQuery, ignoreCase = true)
        val matchesCategory = selectedCategory.isBlank() ||
            event.category.equals(selectedCategory, ignoreCase = true)
        val matchesDate = selectedDate.isBlank() || event.date == selectedDate
        matchesName && matchesCategory && matchesDate
    }.filterNot { it.hasEnded() }
        .sortedWith(compareBy<Event> { it.date }.thenBy { it.time })

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Explorar eventos", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }

                },
                actions = {
                    if (searchQuery.isNotBlank() || selectedCategory.isNotBlank() || selectedDate.isNotBlank()) {
                        TextButton(onClick = {
                            searchQuery = ""
                            selectedCategory = ""
                            selectedDate = ""
                        }) {
                            Text("Limpiar", color = Color(0xFF567C8D))
                        }
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Encuentra tu próximo plan",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF244B68),
                    )
                    Text(
                        "Explora eventos próximos y descubre nuevas experiencias.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Buscar por nombre, lugar o descripción") },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, "Borrar búsqueda")
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize(),
                        singleLine = true,
                        shape = RoundedCornerShape(18.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF567C8D),
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        ),
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    AssistChip(
                        onClick = {
                            val calendar = Calendar.getInstance()
                            DatePickerDialog(
                                context,
                                { _, year, month, day ->
                                    selectedDate = String.format(
                                        Locale.US,
                                        "%04d-%02d-%02d",
                                        year,
                                        month + 1,
                                        day,
                                    )
                                },
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH),
                            ).show()
                        },
                        label = {
                            Text(if (selectedDate.isBlank()) "Fecha" else selectedDate)
                        },
                        leadingIcon = { Icon(Icons.Default.CalendarToday, null, Modifier.size(18.dp)) },
                        shape = RoundedCornerShape(14.dp),
                    )
                    categories.forEach { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = {
                                selectedCategory = if (selectedCategory == category) "" else category
                            },
                            label = { Text(category) },
                            leadingIcon = {
                                if (selectedCategory == category) {
                                    Icon(Icons.Default.Check, null, Modifier.size(16.dp))
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                        )
                    }
                }
            }

            item {
                Text(
                    "${filteredEvents.size} evento${if (filteredEvents.size == 1) "" else "s"} disponible${if (filteredEvents.size == 1) "" else "s"}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF244B68),
                )
            }

            if (state.isLoading && state.events.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = Color(0xFF244B68))
                    }
                }
            } else if (filteredEvents.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Icon(
                                Icons.Default.EventBusy,
                                contentDescription = null,
                                modifier = Modifier.size(44.dp),
                                tint = Color(0xFF567C8D),
                            )
                            Text(
                                "No encontramos eventos",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                "Prueba con otra búsqueda o elimina alguno de los filtros.",
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            } else {
                itemsIndexed(
                    filteredEvents,
                    key = { index, event -> event.id ?: "search-$index" },
                ) { _, event ->
                    SearchEventCard(event) {
                        event.id?.let { navController.navigate("event_detail/${Uri.encode(it)}") }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchEventCard(event: Event, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (event.coverImage.isNotBlank()) {
                AsyncImage(
                    model = event.coverImage.fastImageUrl(),
                    contentDescription = event.title,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(14.dp)),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFDDEAF0)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Event, null, tint = Color(0xFF406370))
                }

            }
            Spacer(Modifier.width(14.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Text(
                    event.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF244B68),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                EventInfoLine(
                    Icons.Default.CalendarToday,
                    "${event.date} · ${event.time.ifBlank { "Hora no definida" }}",
                )
                EventInfoLine(
                    Icons.Default.LocationOn,
                    event.place.ifBlank { "Lugar por confirmar" },
                )
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (event.availableSlots > 0) {
                        Color(0xFFDDF2E9)
                    } else {
                        MaterialTheme.colorScheme.errorContainer
                    },
                ) {
                    Text(
                        if (event.availableSlots > 0) {
                            "${event.availableSlots} cupos disponibles"
                        } else {
                            "Sin cupos disponibles"
                        },
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (event.availableSlots > 0) Color(0xFF27705D) else MaterialTheme.colorScheme.error,
                    )
                }
            }

        }
    }
}
