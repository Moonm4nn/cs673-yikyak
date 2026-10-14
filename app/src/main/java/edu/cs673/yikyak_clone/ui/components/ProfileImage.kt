package edu.cs673.yikyak_clone.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import edu.cs673.yikyak_clone.R

/*@Preview
@Composable
fun ProfileImage() {
    Image (
        painter = painterResource(id = R.drawable.ic_launcher_background),
        contentDescription = "formatted profile image",
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .size(120.dp)
            .clip(CircleShape)
            .border(2.dp, Color.Gray, CircleShape)
    )
}*/

@Preview
@Composable
fun ProfileImage(
    modifier: Modifier = Modifier,
    @DrawableRes imageId: Int = R.drawable.ic_launcher_background,
    contentDescription: String? = "formatted profile image"
) {
    Image(
        painter = painterResource(id = imageId),
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .size(120.dp)
            .clip(CircleShape)
            .border(2.dp, Color.Gray, CircleShape)
    )
}
