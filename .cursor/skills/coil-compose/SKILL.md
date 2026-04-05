---
name: coil-compose
description: Expert guidance on using Coil 2.7.0 for image loading in Jetpack Compose. Covers loading from local file paths, handling image states, and optimizing image performance. CardVault loads images from device storage, not URLs.
---

# Coil for Jetpack Compose (Local File Loading)

## Instructions

CardVault uses **Coil 2.7.0** for loading card images from **local file storage** (not URLs). All images are stored in the app's private directory (`context.filesDir/images/`).

### 1. Primary Composable: `AsyncImage` (Local Files)

Use `AsyncImage` with `File` objects or file path strings for local image loading:

```kotlin
import java.io.File

@Composable
fun CardImage(
    imagePath: String,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(File(imagePath))
            .crossfade(true)
            .build(),
        placeholder = painterResource(R.drawable.card_placeholder),
        error = painterResource(R.drawable.card_error),
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        modifier = modifier.clip(RoundedCornerShape(12.dp))
    )
}
```

### 2. Front/Back Card Image Pattern

CardVault stores separate front and back images per card:

```kotlin
@Composable
fun CardImageDisplay(
    frontImagePath: String,
    backImagePath: String,
    showFront: Boolean,
    modifier: Modifier = Modifier
) {
    val imagePath = if (showFront) frontImagePath else backImagePath

    if (imagePath.isNotBlank()) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(File(imagePath))
                .crossfade(true)
                .size(Size.ORIGINAL)
                .build(),
            contentDescription = if (showFront) "Card front" else "Card back",
            contentScale = ContentScale.Fit,
            modifier = modifier
        )
    } else {
        Box(
            modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.CreditCard, contentDescription = "No image")
        }
    }
}
```

### 3. Validating Image Files

Always check that the file exists before loading:

```kotlin
@Composable
fun SafeCardImage(
    imagePath: String,
    modifier: Modifier = Modifier
) {
    val file = remember(imagePath) { File(imagePath) }
    val fileExists = remember(imagePath) { file.exists() && file.length() > 0 }

    if (fileExists) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(file)
                .crossfade(true)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    } else {
        PlaceholderImage(modifier = modifier)
    }
}
```

### 4. Low-Level Control: `rememberAsyncImagePainter`

Use when you need a `Painter` object (e.g., for `FlippableCard`, `CardFront`, `CardBack`):

```kotlin
val painter = rememberAsyncImagePainter(
    model = ImageRequest.Builder(LocalContext.current)
        .data(File(imagePath))
        .size(Size.ORIGINAL)
        .build()
)

Image(
    painter = painter,
    contentDescription = "Card image",
    modifier = Modifier.fillMaxWidth()
)
```

> **Warning**: `rememberAsyncImagePainter` does not auto-detect display size. Use `AsyncImage` unless a `Painter` is specifically needed.

### 5. Thumbnail Loading for Lists

In `LazyColumn`/`LazyGrid`, use smaller sizes for thumbnails to save memory:

```kotlin
@Composable
fun CardThumbnail(
    imagePath: String,
    modifier: Modifier = Modifier
) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(File(imagePath))
            .crossfade(true)
            .size(200, 120)  // Thumbnail size
            .build(),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .size(width = 100.dp, height = 60.dp)
            .clip(RoundedCornerShape(8.dp))
    )
}
```

### 6. Performance Best Practices

*   **Singleton ImageLoader**: Use a single `ImageLoader` instance (Coil provides one by default).
*   **Main-Safe**: Coil decodes images on background threads automatically.
*   **Crossfade**: Always use `crossfade(true)` for smoother transitions.
*   **Size Constraints**: Use `.size()` in `ImageRequest` for lists to avoid loading full-resolution images.
*   **Memory**: Card images are JPEG quality 85, already compressed by `ImageFileManager`.
*   **Avoid SubcomposeAsyncImage** in `LazyColumn`/`LazyRow` — subcomposition is slower.
*   **File validation**: Check `file.exists()` before loading to avoid error states.

### 7. Checklist
- [ ] Prefer `AsyncImage` over other variants.
- [ ] Load from `File(imagePath)`, not URL strings.
- [ ] Always provide meaningful `contentDescription` or `null` for decorative images.
- [ ] Use `crossfade(true)` for smooth transitions.
- [ ] Avoid `SubcomposeAsyncImage` in lists.
- [ ] Use `.size()` constraints in `ImageRequest` for thumbnails.
- [ ] Validate file existence with `remember` before loading.
- [ ] Provide placeholder and error drawables for all card images.
