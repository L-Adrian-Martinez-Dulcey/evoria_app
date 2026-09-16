@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.p3.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import android.net.Uri
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.p3.data.model.Event
import com.example.p3.data.model.User
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
    EventFeedback(state.error, state.message) { viewModel.clearMessage() }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Evoria") },
                actions = {
                    IconButton(onClick = { navController.navigate("event_search") }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Buscar eventos"
                        )
                    }
                    IconButton(onClick = { userViewModel.toggleDarkMode() }) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.WbSunny else Icons.Default.DarkMode,
                            contentDescription = "Cambiar modo de tema"
                        )
                    }
                }
            )
        },
        floatingActionButton = { FloatingActionButton(onClick = { navController.navigate("event_form") }) { Icon(Icons.Default.Add, "Crear evento") } }
    ) { padding ->
        when {
            state.isLoading && state.events.isEmpty() -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            state.events.isEmpty() -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { Text("No hay eventos disponibles") }
            else -> LazyColumn(Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                itemsIndexed(state.events, key = { index, event -> event.id ?: "event-$index" }) { _, event ->
                    EventCard(event) {
                        event.id?.let { navController.navigate("event_detail/${Uri.encode(it)}") }
                    }
                }
            }
        }
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
                    model = event.coverImage,
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
    val creatorName = users.firstOrNull { it.id == event.creatorId }?.name
        ?: if (event.creatorId == user.id) user.name else "Organizador de EVORIA"
    val averageRating = event.reviews
        .map { it.rating }
        .takeIf { it.isNotEmpty() }
        ?.average()
    var qrBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var qrError by remember { mutableStateOf<String?>(null) }
    val detailSnackbarHostState = remember { SnackbarHostState() }

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
                        model = event.coverImage,
                        contentDescription = "Imagen de ${event.title}",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(230.dp)
                            .clip(RoundedCornerShape(24.dp)),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(230.dp),
                        shape = RoundedCornerShape(24.dp),
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
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        event.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2F4156),
                    )
                    Text(
                        "Organizado por $creatorName",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
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

            item {
                EventInfoGrid(event)
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

            if (event.reviews.isNotEmpty()) {
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
                                else viewModel.register(event, user.id.orEmpty())
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
                        if (isRegistered && hasFinished(event.date)) {
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
    if (confirmDelete) AlertDialog(onDismissRequest = { confirmDelete = false }, title = { Text("¿Eliminar evento?") }, text = { Text("Esta acción no se puede deshacer.") }, confirmButton = { TextButton({ viewModel.delete(event) { navController.popBackStack() } }) { Text("Eliminar") } }, dismissButton = { TextButton({ confirmDelete = false }) { Text("Cancelar") } })
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
                        model = image,
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
    val events by viewModel.uiState.collectAsState(); var tab by remember { mutableStateOf(0) }
    val list = if (tab == 0) events.events.filter { it.creatorId == user.id } else events.events.filter { event -> event.registrations.any { it.userId == user.id } }
    Column { TabRow(tab) { listOf("Creados", "Inscritos").forEachIndexed { index, text -> Tab(selected = tab == index, onClick = { tab = index }, text = { Text(text) }) } }; LazyColumn(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { itemsIndexed(list, key = { index, event -> event.id ?: "my-event-$index" }) { _, event -> EventCard(event) { event.id?.let { navController.navigate("event_detail/$it") } } } } }
}

@Composable
fun ProfileScreen(user: User, userViewModel: UserViewModel, navController: NavController) {
    val context = LocalContext.current
    val eventViewModel: EventViewModel = viewModel()
    val eventState by eventViewModel.uiState.collectAsState()
    val profileError by userViewModel.error.collectAsState()
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
            event.registrations.any { it.userId == user.id } && isWithinNextSevenDays(event.date)
        }
        .sortedWith(compareBy<Event> { it.date }.thenBy { it.time })
    val popularEvents = eventState.events
        .filter { event ->
            event.creatorId == user.id &&
                event.reviews.isNotEmpty() &&
                event.reviews.map { it.rating }.average() in 4.5..5.0
        }
        .sortedByDescending { event -> event.reviews.map { it.rating }.average() }

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
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, top = 16.dp, end = 20.dp, bottom = 28.dp),
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
                ProfileSectionCard(
                    title = "Populares",
                    subtitle = "Mis eventos con valoración de 4.5 a 5.0",
                    icon = Icons.Default.Star,
                    expanded = popularExpanded,
                    onClick = { popularExpanded = !popularExpanded }
                ) {
                    AnimatedVisibility(
                        visible = popularExpanded,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (popularEvents.isEmpty()) {
                                Text(
                                    "No tienes eventos populares todavía.",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            } else {
                                popularEvents.forEach { event ->
                                    UpcomingEventCard(event) {
                                        event.id?.let { navController.navigate("event_detail/${Uri.encode(it)}") }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                ProfileSectionCard(
                    title = "Próximos",
                    subtitle = "Mis eventos de los próximos 7 días",
                    icon = Icons.Default.Event,
                    expanded = upcomingExpanded,
                    onClick = { upcomingExpanded = !upcomingExpanded }
                ) {
                    AnimatedVisibility(
                        visible = upcomingExpanded,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            when {
                                eventState.isLoading && eventState.events.isEmpty() -> {
                                    Box(
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 20.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(color = Color(0xFF567C8D))
                                    }
                                }

                                upcomingEvents.isEmpty() -> {
                                    Text(
                                        "No tienes eventos próximos esta semana.",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }

                                else -> upcomingEvents.forEach { event ->
                                    UpcomingEventCard(event) {
                                        event.id?.let { navController.navigate("event_detail/${Uri.encode(it)}") }
                                    }
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
                        model = avatar,
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
                    model = event.coverImage,
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

private fun isWithinNextSevenDays(date: String): Boolean = runCatching {
    val format = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { isLenient = false }
    val eventDate = format.parse(date) ?: return false
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

@Composable private fun EventFeedback(error: String?, message: String?, clear: () -> Unit) { if (error != null || message != null) LaunchedEffect(error, message) { /* El estado se visualiza en cada pantalla sin ocultar errores. */ } }
@Composable private fun ReviewDialog(onDismiss: () -> Unit, save: (Int, String) -> Unit) { var rating by remember { mutableStateOf("") }; var comment by remember { mutableStateOf("") }; AlertDialog(onDismissRequest = onDismiss, title = { Text("Calificar evento") }, text = { Column { AppField(rating, { rating = it }, "Puntaje (1-5)", KeyboardType.Number); AppField(comment, { comment = it }, "Comentario", single = false) } }, confirmButton = { TextButton({ save(rating.toIntOrNull() ?: 0, comment) }) { Text("Publicar") } }, dismissButton = { TextButton(onDismiss) { Text("Cancelar") } }) }
private fun now() = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(Calendar.getInstance().time)
private fun hasFinished(date: String) = runCatching { SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(date)?.before(Calendar.getInstance().time) == true }.getOrDefault(false)

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
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf("") }
    
    val categories = listOf("Arte", "Deporte", "Tecnología", "Música", "Gastronomía", "Educación")
    
    // Filtrado combinado reactivo
    val filteredEvents = state.events.filter { event ->
        val matchesName = event.title.contains(searchQuery, ignoreCase = true) || 
                          event.description.contains(searchQuery, ignoreCase = true)
        val matchesCategory = selectedCategory.isBlank() || event.category.equals(selectedCategory, ignoreCase = true)
        val matchesDate = selectedDate.isBlank() || event.date == selectedDate
        
        matchesName && matchesCategory && matchesDate
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Buscar Eventos") },
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
                            Text("Limpiar", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Campo de búsqueda por Nombre/Texto
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Buscar por nombre") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Selector de Fecha
                val context = LocalContext.current
                OutlinedButton(
                    onClick = {
                        val c = Calendar.getInstance()
                        DatePickerDialog(context, { _, y, m, d ->
                            selectedDate = String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d)
                        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = if (selectedDate.isBlank()) "Fecha" else selectedDate,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Selector de Categoría (Menú desplegable simple)
                var catExpanded by remember { mutableStateOf(false) }
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedButton(
                        onClick = { catExpanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (selectedCategory.isBlank()) "Categoría" else selectedCategory,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    DropdownMenu(expanded = catExpanded, onDismissRequest = { catExpanded = false }) {
                        DropdownMenuItem(
                            text = { Text("Todas") },
                            onClick = { selectedCategory = ""; catExpanded = false }
                        )
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = { selectedCategory = cat; catExpanded = false }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Resultados
            if (filteredEvents.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No se encontraron eventos con los filtros seleccionados.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(filteredEvents, key = { index, event -> event.id ?: "search-$index" }) { _, event ->
                        EventCard(event) {
                            event.id?.let { navController.navigate("event_detail/${Uri.encode(it)}") }
                        }
                    }
                }
            }
        }
    }
}
