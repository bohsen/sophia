import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowRight
import androidx.compose.material.icons.outlined.AccountBox
import androidx.compose.material.icons.outlined.AccountTree
import androidx.compose.material.icons.outlined.Contacts
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.FrameWindowScope
import theme.*

@ExperimentalMaterialApi
@Composable
fun FrameWindowScope.SettingsScreen(onCloseRequest: () -> Unit) {
    val keyValueStore = KeyValueStore()
    val openSetDirectory = remember { mutableStateOf(false) }
    val openPipelinesSetting = remember { mutableStateOf(false) }
    val tokenCardRead = remember { mutableStateOf(false) }

    Surface(color = BackgroundColor, modifier = Modifier.fillMaxSize()) {
        Column {
            HeaderText()
            ProfileCardUI(onTokencardRead = { tokenCardRead.value = true })
            GeneralOptionsUI(onSetDirectory = { openSetDirectory.value = true },
                onOpenPipelinesSetting = { openPipelinesSetting.value = true })
            SupportOptionsUI()
        }
    }

    if (tokenCardRead.value) {
        FileChooserDialog("Vælg tokencard", keyValueStore.path, {}, onCloseRequest = { tokenCardRead.value = false })
    }

    if (openSetDirectory.value) {
        FileChooserDialog(
            "Vælg mappe",
            keyValueStore.path,
            { path -> keyValueStore.path = path },
            onCloseRequest = { openSetDirectory.value = false })
    }
}

@Composable
fun HeaderText() {
    Text(
        text = "Indstillinger",
        fontFamily = Poppins,
        color = SecondaryColor,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 30.dp, bottom = 10.dp),
        fontWeight = FontWeight.ExtraBold,
        fontSize = 16.sp
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ProfileCardUI(onTokencardRead: () -> Unit) {
    Column(
        modifier = Modifier
            .padding(horizontal = 14.dp)
            .padding(top = 10.dp)
    ) {
        Text(
            text = "Profil",
            fontFamily = Poppins,
            color = SecondaryColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(vertical = 8.dp)
        )
        ProfileSettingItem(
            icon = Icons.Outlined.AccountBox,
            mainText = "Brugernavn",
            subText = "Ikke logget ind",
            onTokencardRead = onTokencardRead
        )
    }
}

@ExperimentalMaterialApi
@Composable
fun ProfileSettingItem(icon: ImageVector, mainText: String, subText: String, onTokencardRead: () -> Unit) {
    Card(
        backgroundColor = Color.White,
        modifier = Modifier
            .padding(bottom = 8.dp)
            .fillMaxWidth(),
        elevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BadgedBox(
                    badge = { Badge { Text("!") } })
                {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(shape = Shapes.medium)
                            .background(LightPrimaryColor)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = "",
                            tint = Color.Unspecified,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(
                    modifier = Modifier.offset(y = (2).dp)
                ) {
                    Text(
                        text = mainText,
                        fontFamily = Poppins,
                        color = SecondaryColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                    )

                    Text(
                        text = subText,
                        fontFamily = Poppins,
                        color = Color.Gray,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.offset(y = (-4).dp)
                    )
                }
            }
            Button(
                modifier = Modifier.padding(top = 10.dp),
                onClick = { onTokencardRead() },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = PrimaryColor
                ),
                contentPadding = PaddingValues(horizontal = 30.dp),
                elevation = ButtonDefaults.elevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 2.dp
                ),
                shape = Shapes.medium
            ) {
                Text(
                    text = "Token card",
                    fontFamily = Poppins,
                    color = SecondaryColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@ExperimentalMaterialApi
@Preview
@Composable
fun GeneralOptionsUI(
    onSetDirectory: () -> Unit,
    onOpenPipelinesSetting: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 14.dp)
            .padding(top = 10.dp)
    ) {
        Text(
            text = "Generelt",
            fontFamily = Poppins,
            color = SecondaryColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(vertical = 8.dp)
        )
        GeneralSettingItem(
            icon = Icons.Outlined.FolderOpen,
            mainText = "Analysemappe",
            subText = "Sti til mappe, hvor analyseresultater gemmes"
        ) { onSetDirectory() }
        GeneralSettingItem(
            icon = Icons.Outlined.AccountTree,
            mainText = "Pipelines",
            subText = "Konfiguration af pipelines"
        ) { onOpenPipelinesSetting() }
    }
}

@ExperimentalMaterialApi
@Composable
fun GeneralSettingItem(icon: ImageVector, mainText: String, subText: String, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        backgroundColor = Color.White,
        modifier = Modifier
            .padding(bottom = 8.dp)
            .fillMaxWidth(),
        elevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(shape = Shapes.medium)
                        .background(LightPrimaryColor)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = "",
                        tint = Color.Unspecified,
                        modifier = Modifier.padding(8.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))
                Column(
                    modifier = Modifier.offset(y = 2.dp)
                ) {
                    Text(
                        text = mainText,
                        fontFamily = Poppins,
                        color = SecondaryColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                    )

                    Text(
                        text = subText,
                        fontFamily = Poppins,
                        color = Color.Gray,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.offset(y = (-4).dp)
                    )
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowRight,
                contentDescription = "",
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@ExperimentalMaterialApi
@Composable
fun SupportOptionsUI() {
    Column(
        modifier = Modifier
            .padding(horizontal = 14.dp)
            .padding(top = 10.dp)
    ) {
        Text(
            text = "Support",
            fontFamily = Poppins,
            color = SecondaryColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(vertical = 8.dp)
        )
        SupportItem(
            icon = Icons.Outlined.Contacts,
            mainText = "Kontakt",
            onClick = {}
        )
    }
}

@ExperimentalMaterialApi
@Composable
fun SupportItem(icon: ImageVector, mainText: String, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        backgroundColor = Color.White,
        modifier = Modifier
            .padding(bottom = 8.dp)
            .fillMaxWidth(),
        elevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(shape = Shapes.medium)
                        .background(LightPrimaryColor)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = "",
                        tint = Color.Unspecified,
                        modifier = Modifier.padding(8.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Text(
                    text = mainText,
                    fontFamily = Poppins,
                    color = SecondaryColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowRight,
                contentDescription = "",
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun PipelinesSettingUI() {
    Surface(color = BackgroundColor, modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .padding(horizontal = 14.dp)
                .padding(top = 10.dp)
        ) {
            Card(
                backgroundColor = Color.White,
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .fillMaxWidth(),
                elevation = 0.dp,
            ) {
                Column {
                    SettingsHeader("Mapning", "Forklarende tekst")
                    PipelineItem(Pair("LRT", Pipeline(1811)), {})
                    PipelineItem(Pair("QGT", Pipeline(1811)), {})
                    PipelineItem(Pair("KSK", Pipeline(1811)), {})

                }
            }
        }
    }
}

@Composable
fun SettingsHeader(mainText: String, subText: String) {
    Row(
        modifier = Modifier.padding(vertical = 10.dp, horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.offset(y = 2.dp)
        ) {
            Text(
                text = mainText,
                fontFamily = Poppins,
                color = SecondaryColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
            )

            Text(
                text = subText,
                fontFamily = Poppins,
                color = Color.Gray,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.offset(y = (-4).dp)
            )
        }

    }
}


@Composable
fun PipelineItem(pipeline: Pair<String, Pipeline>, onPipelineClick: (String) -> Unit) {
    Card(border = BorderStroke(1.dp, PrimaryColor)) {
        Row(
            modifier = Modifier
                .padding(vertical = 8.dp, horizontal = 14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                modifier = Modifier
                    .padding(8.dp)
                    .weight(0.25F),
                text = pipeline.first
            )
            Text(
                modifier = Modifier
                    .padding(8.dp)
                    .weight(0.75F),
                text = "Testpipeline"
            )
        }
    }
}

@Preview
@Composable
fun PipelineItemPreview() {
    PipelinesSettingUI()
}

@OptIn(ExperimentalMaterialApi::class)
@Preview
@Composable
fun SupportItemPreview() {
    Column {
        GeneralOptionsUI({}, {})
        SupportOptionsUI()
    }
}
