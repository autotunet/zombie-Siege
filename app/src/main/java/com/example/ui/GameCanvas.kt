package com.example.ui

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import com.example.model.GroundDecal
import com.example.model.Particle
import com.example.model.Projectile
import com.example.model.ShellCasing
import com.example.model.WeaponType
import com.example.model.Zombie
import com.example.model.ZombieType
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

@Composable
fun GameCanvas(
    zombies: List<Zombie>,
    projectiles: List<Projectile>,
    particles: List<Particle>,
    decals: List<GroundDecal>,
    shells: List<ShellCasing>,
    barricadeCurrentHp: Float,
    barricadeMaxHp: Float,
    barricadeFlashTimer: Float,
    barricadeSpikesLevel: Int,
    screenShakeIntensity: Float,
    aimNormX: Float,
    aimNormY: Float,
    muzzleFlashTimer: Float,
    comboStreak: Int,
    soldierNormX: Float = 0.5f,
    soldierWalkAnim: Float = 0f,
    selectedWeaponType: WeaponType = WeaponType.PISTOL,
    onShootTarget: (normX: Float, normY: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val textPaint = remember {
        Paint().apply {
            isAntiAlias = true
            textSize = 34f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
            setShadowLayer(8f, 0f, 2f, android.graphics.Color.BLACK)
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { offset ->
                        val normX = (offset.x / size.width).coerceIn(0f, 1f)
                        val normY = (offset.y / size.height).coerceIn(0f, 1f)
                        onShootTarget(normX, normY)
                    }
                )
            }
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    val normX = (change.position.x / size.width).coerceIn(0f, 1f)
                    val normY = (change.position.y / size.height).coerceIn(0f, 1f)
                    onShootTarget(normX, normY)
                }
            }
    ) {
        val width = size.width
        val height = size.height
        val barricadeY = height * 0.86f

        // Smooth damped harmonic screen shake
        val shakeX = if (screenShakeIntensity > 0f) (sin(System.currentTimeMillis() * 0.05f) * screenShakeIntensity * 0.8f).toFloat() else 0f
        val shakeY = if (screenShakeIntensity > 0f) (cos(System.currentTimeMillis() * 0.06f) * screenShakeIntensity * 0.8f).toFloat() else 0f

        // 1. Realistic Urban City Street Environment (Sidewalks, curbs, crosswalks, manholes, parked wrecks)
        drawRealisticCityStreet(width, height, barricadeY, shakeX, shakeY)

        // 2. Persistent Ground Decals (Organic Blood Pools & Scorch Marks)
        decals.forEach { decal ->
            drawSoftGroundDecal(decal, width, height, shakeX, shakeY)
        }

        // 3. Realistic Streetlamps & Ambient Volumetric Lighting
        drawStreetlampLighting(width, height, barricadeY, shakeX, shakeY)

        // 4. Photorealistic Anatomical Living Zombies
        zombies.forEach { zombie ->
            drawRealisticZombie(zombie, width, height, shakeX, shakeY)
        }

        // 5. Ballistic Projectiles & Tracers with Smooth Glow
        projectiles.forEach { p ->
            drawBallisticProjectile(p, width, height, shakeX, shakeY)
        }

        // 6. Realistic Brass Shell Casings with Spin & Shine
        shells.forEach { shell ->
            drawRealisticShellCasing(shell, width, height, shakeX, shakeY)
        }

        // 7. Military Tactical Holographic Reticle & Laser Sighting
        drawModernHolographicReticle(
            aimNormX = aimNormX,
            aimNormY = aimNormY,
            soldierNormX = soldierNormX,
            width = width,
            height = height,
            barricadeY = barricadeY,
            shakeX = shakeX,
            shakeY = shakeY
        )

        // 8. Heavy Ballistic Barricade (Sandbags, Armor Plates, Barbed Wire & Living Soldier)
        drawRealisticMilitaryBarricade(
            width = width,
            height = height,
            barricadeY = barricadeY,
            currentHp = barricadeCurrentHp,
            maxHp = barricadeMaxHp,
            flashTimer = barricadeFlashTimer,
            spikesLevel = barricadeSpikesLevel,
            muzzleFlashTimer = muzzleFlashTimer,
            aimNormX = aimNormX,
            aimNormY = aimNormY,
            soldierNormX = soldierNormX,
            soldierWalkAnim = soldierWalkAnim,
            weaponType = selectedWeaponType,
            shakeX = shakeX,
            shakeY = shakeY
        )

        // 9. Floating Combat Text & Particle Effects with Soft Easing
        particles.forEach { particle ->
            val px = particle.x * width + shakeX
            val py = particle.y * height + shakeY

            if (particle.text != null) {
                textPaint.color = particle.color.hashCode()
                textPaint.alpha = (particle.alpha * 255).toInt().coerceIn(0, 255)
                drawContext.canvas.nativeCanvas.drawText(
                    particle.text,
                    px,
                    py,
                    textPaint
                )
            } else {
                drawCircle(
                    color = particle.color.copy(alpha = particle.alpha),
                    radius = particle.size * particle.life,
                    center = Offset(px, py)
                )
            }
        }

        // 10. Smooth Cinematic Atmosphere & Vignette
        drawSoftAtmosphericEmbers(width, height)
        drawAmbientAtmosphericVignette(width, height)
    }
}

/**
 * Renders a full, photorealistic urban city street:
 * - Left and right concrete sidewalks with slab seams and 3D curbs
 * - Wet distressed asphalt roadway with rain puddle reflections
 * - Weathered white pedestrian crosswalk
 * - Double yellow center division lines
 * - Heavy cast-iron sewer manhole covers with concentric ribs
 * - Abandoned police cruiser and burnt civilian wreck on the roadside
 */
private fun DrawScope.drawRealisticCityStreet(
    width: Float,
    height: Float,
    barricadeY: Float,
    shakeX: Float,
    shakeY: Float
) {
    val sidewalkWidth = width * 0.15f
    val roadLeft = sidewalkWidth
    val roadRight = width - sidewalkWidth

    // 1. Asphalt Road Base with Subtle Wet Night Sheen
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF0A0D12),
                Color(0xFF131820),
                Color(0xFF181F2A),
                Color(0xFF11161E)
            ),
            startY = 0f,
            endY = height
        )
    )

    // 2. Left Sidewalk (Concrete pavement slabs)
    drawRect(
        brush = Brush.horizontalGradient(
            colors = listOf(Color(0xFF1E242C), Color(0xFF2B323C)),
            startX = 0f,
            endX = roadLeft
        ),
        topLeft = Offset(0f + shakeX, 0f),
        size = Size(roadLeft, barricadeY)
    )
    // Sidewalk Slab Divider Seams (Horizontal joint lines every 60px)
    var seamY = 0f
    while (seamY < barricadeY) {
        drawLine(
            color = Color(0x33000000),
            start = Offset(0f + shakeX, seamY + shakeY),
            end = Offset(roadLeft + shakeX, seamY + shakeY),
            strokeWidth = 2f
        )
        drawLine(
            color = Color(0x22FFFFFF),
            start = Offset(0f + shakeX, seamY + 1.5f + shakeY),
            end = Offset(roadLeft + shakeX, seamY + 1.5f + shakeY),
            strokeWidth = 1f
        )
        seamY += 65f
    }
    // Left Curb (3D Bevel with highlight and contact drop shadow)
    drawRect(
        color = Color(0xFF45505E),
        topLeft = Offset(roadLeft - 6f + shakeX, 0f),
        size = Size(6f, barricadeY)
    )
    drawLine(
        color = Color(0xFF8895A5),
        start = Offset(roadLeft - 6f + shakeX, 0f),
        end = Offset(roadLeft - 6f + shakeX, barricadeY),
        strokeWidth = 1.5f
    )
    // Gutter drop shadow
    drawRect(
        color = Color(0x66000000),
        topLeft = Offset(roadLeft + shakeX, 0f),
        size = Size(8f, barricadeY)
    )

    // 3. Right Sidewalk (Concrete pavement slabs)
    drawRect(
        brush = Brush.horizontalGradient(
            colors = listOf(Color(0xFF2B323C), Color(0xFF1E242C)),
            startX = roadRight,
            endX = width
        ),
        topLeft = Offset(roadRight + shakeX, 0f),
        size = Size(sidewalkWidth, barricadeY)
    )
    // Right Sidewalk slab seams
    seamY = 0f
    while (seamY < barricadeY) {
        drawLine(
            color = Color(0x33000000),
            start = Offset(roadRight + shakeX, seamY + shakeY),
            end = Offset(width + shakeX, seamY + shakeY),
            strokeWidth = 2f
        )
        drawLine(
            color = Color(0x22FFFFFF),
            start = Offset(roadRight + shakeX, seamY + 1.5f + shakeY),
            end = Offset(width + shakeX, seamY + 1.5f + shakeY),
            strokeWidth = 1f
        )
        seamY += 65f
    }
    // Right Curb 3D Bevel
    drawRect(
        color = Color(0xFF45505E),
        topLeft = Offset(roadRight + shakeX, 0f),
        size = Size(6f, barricadeY)
    )
    drawLine(
        color = Color(0xFF8895A5),
        start = Offset(roadRight + 6f + shakeX, 0f),
        end = Offset(roadRight + 6f + shakeX, barricadeY),
        strokeWidth = 1.5f
    )
    // Gutter drop shadow
    drawRect(
        color = Color(0x66000000),
        topLeft = Offset(roadRight - 8f + shakeX, 0f),
        size = Size(8f, barricadeY)
    )

    // 4. Pedestrian Crosswalk (Paso de Cebra) across road at Y ~ 36%
    val crosswalkY = height * 0.36f + shakeY
    val stripeWidth = 18f
    val stripeHeight = 65f
    val stripeSpacing = 28f
    var stripeX = roadLeft + 22f + shakeX
    while (stripeX < roadRight - 22f) {
        // Weathered white stripe with realistic distressed edges
        drawRoundRect(
            color = Color(0x88E2E8F0),
            topLeft = Offset(stripeX, crosswalkY),
            size = Size(stripeWidth, stripeHeight),
            cornerRadius = CornerRadius(2f, 2f)
        )
        // Tire scuff wear in middle of stripe
        drawRect(
            color = Color(0x22000000),
            topLeft = Offset(stripeX + 2f, crosswalkY + 15f),
            size = Size(stripeWidth - 4f, stripeHeight - 30f)
        )
        stripeX += stripeWidth + stripeSpacing
    }

    // 5. Double Yellow Center Line (Líneas continuas reflectantes desgastadas)
    val centerX = width * 0.5f + shakeX
    val yellowDash = 50f
    val yellowGap = 40f
    var yPos = 10f
    while (yPos < barricadeY) {
        // Skip crosswalk area for realistic road layout
        if (yPos < crosswalkY - 20f || yPos > crosswalkY + stripeHeight + 20f) {
            drawLine(
                color = Color(0x66FBC02D),
                start = Offset(centerX - 4.5f, yPos + shakeY),
                end = Offset(centerX - 4.5f, yPos + yellowDash + shakeY),
                strokeWidth = 3f
            )
            drawLine(
                color = Color(0x66FBC02D),
                start = Offset(centerX + 4.5f, yPos + shakeY),
                end = Offset(centerX + 4.5f, yPos + yellowDash + shakeY),
                strokeWidth = 3f
            )
        }
        yPos += yellowDash + yellowGap
    }

    // 6. Cast-Iron Sewer Manhole Covers (Tapas de Alcantarilla)
    drawManholeCover(Offset(width * 0.35f + shakeX, height * 0.62f + shakeY), 24f)
    drawManholeCover(Offset(width * 0.68f + shakeX, height * 0.18f + shakeY), 22f)

    // 7. Wet Asphalt Rain Puddle Reflections
    drawRainPuddle(Offset(width * 0.38f + shakeX, height * 0.48f + shakeY), 55f, 32f)
    drawRainPuddle(Offset(width * 0.62f + shakeX, height * 0.72f + shakeY), 65f, 36f)

    // 8. Abandoned Vehicles on Roadside
    // Abandoned Police Cruiser on Left Curb
    drawAbandonedPoliceCruiser(Offset(roadLeft - 22f + shakeX, height * 0.22f + shakeY))
    // Burnt Wrecked Civilian Sedan on Right Curb
    drawAbandonedWreck(Offset(roadRight + 24f + shakeX, height * 0.54f + shakeY))
}

/**
 * Detailed Cast-Iron Sewer Manhole Cover with concentric ring ribs
 */
private fun DrawScope.drawManholeCover(center: Offset, radius: Float) {
    // Outer drop rim
    drawCircle(color = Color(0x88000000), radius = radius + 3f, center = center)
    // Iron base
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF37474F), Color(0xFF1E282D)),
            center = center,
            radius = radius
        ),
        radius = radius,
        center = center
    )
    // Concentric grip rings
    drawCircle(color = Color(0xFF263238), radius = radius * 0.75f, center = center, style = Stroke(width = 2f))
    drawCircle(color = Color(0xFF263238), radius = radius * 0.45f, center = center, style = Stroke(width = 2f))
    // Pick hole
    drawCircle(color = Color(0xFF0F171A), radius = radius * 0.15f, center = center)
}

/**
 * Realistic Wet Rain Puddle reflecting ambient streetlights
 */
private fun DrawScope.drawRainPuddle(center: Offset, radiusX: Float, radiusY: Float) {
    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0x5542A5F5), // sky water reflection
                Color(0x331E2A38),
                Color(0x00000000)
            ),
            center = center,
            radius = radiusX
        ),
        topLeft = Offset(center.x - radiusX, center.y - radiusY),
        size = Size(radiusX * 2f, radiusY * 2f)
    )
}

/**
 * Abandoned City Police Cruiser with lightbar and shattered glass
 */
private fun DrawScope.drawAbandonedPoliceCruiser(center: Offset) {
    val carW = 38f
    val carH = 75f

    // Ground Contact Shadow
    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xAA000000), Color(0x00000000)),
            center = Offset(center.x, center.y + 4f),
            radius = carW * 1.3f
        ),
        topLeft = Offset(center.x - carW * 0.7f, center.y - carH * 0.5f),
        size = Size(carW * 1.4f, carH * 1.1f)
    )

    // Car Body (White / Blue doors)
    drawRoundRect(
        color = Color(0xFFECEFF1),
        topLeft = Offset(center.x - carW / 2f, center.y - carH / 2f),
        size = Size(carW, carH),
        cornerRadius = CornerRadius(6f, 6f)
    )
    // Police Blue Side Panels
    drawRect(
        color = Color(0xFF0D47A1),
        topLeft = Offset(center.x - carW / 2f, center.y - 12f),
        size = Size(carW, 24f)
    )
    // Roof & Broken Lightbar
    drawRoundRect(
        color = Color(0xFF263238),
        topLeft = Offset(center.x - carW * 0.38f, center.y - 16f),
        size = Size(carW * 0.76f, 32f),
        cornerRadius = CornerRadius(4f, 4f)
    )
    // Emergency Lightbar (Red / Blue)
    drawRect(color = Color(0xFFD50000), topLeft = Offset(center.x - 12f, center.y - 2f), size = Size(10f, 4f))
    drawRect(color = Color(0xFF00B0FF), topLeft = Offset(center.x + 2f, center.y - 2f), size = Size(10f, 4f))
    // Cracked Windshield
    drawRect(color = Color(0xAA90A4AE), topLeft = Offset(center.x - carW * 0.35f, center.y - 28f), size = Size(carW * 0.7f, 10f))
}

/**
 * Abandoned Burnt Civilian Wreck
 */
private fun DrawScope.drawAbandonedWreck(center: Offset) {
    val carW = 36f
    val carH = 70f

    // Shadow
    drawOval(
        color = Color(0x99000000),
        topLeft = Offset(center.x - carW * 0.65f, center.y - carH * 0.5f),
        size = Size(carW * 1.3f, carH * 1.1f)
    )
    // Burnt Rusted Metal Frame
    drawRoundRect(
        color = Color(0xFF3E2723),
        topLeft = Offset(center.x - carW / 2f, center.y - carH / 2f),
        size = Size(carW, carH),
        cornerRadius = CornerRadius(6f, 6f)
    )
    // Charred Black Roof & Windows
    drawRect(
        color = Color(0xFF1B1B1B),
        topLeft = Offset(center.x - carW * 0.36f, center.y - 16f),
        size = Size(carW * 0.72f, 32f)
    )
    // Rust & Scorched Hood Patches
    drawCircle(color = Color(0xFF5D4037), radius = 8f, center = Offset(center.x, center.y - 22f))
    drawCircle(color = Color(0xFF212121), radius = 6f, center = Offset(center.x, center.y + 18f))
}

/**
 * Real Streetlamp Light Pools that smoothly illuminate the road
 */
private fun DrawScope.drawStreetlampLighting(
    width: Float,
    height: Float,
    barricadeY: Float,
    shakeX: Float,
    shakeY: Float
) {
    val lampPositions = listOf(
        Offset(width * 0.12f + shakeX, height * 0.28f + shakeY),
        Offset(width * 0.88f + shakeX, height * 0.28f + shakeY),
        Offset(width * 0.12f + shakeX, height * 0.68f + shakeY),
        Offset(width * 0.88f + shakeX, height * 0.68f + shakeY)
    )

    lampPositions.forEach { pos ->
        // Warm Streetlamp Light Cone on the asphalt
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0x35FFE082), // warm streetlight halo
                    Color(0x18FFB300),
                    Color(0x00000000)
                ),
                center = pos,
                radius = 110f
            ),
            radius = 110f,
            center = pos
        )
        // Streetlamp metallic post on sidewalk
        drawCircle(color = Color(0xFF263238), radius = 4f, center = pos)
        drawCircle(color = Color(0xFFFFD54F), radius = 2.5f, center = pos)
    }
}

/**
 * Organic Blood Pools & Scorch Marks
 */
private fun DrawScope.drawSoftGroundDecal(
    decal: GroundDecal,
    width: Float,
    height: Float,
    shakeX: Float,
    shakeY: Float
) {
    val dx = decal.x * width + shakeX
    val dy = decal.y * height + shakeY
    val r = decal.radius

    if (decal.isScorch) {
        // Multi-layered blast crater
        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xF00A0A0A), Color(0x991E1E1E), Color(0x00000000)),
                center = Offset(dx, dy),
                radius = r
            ),
            topLeft = Offset(dx - r, dy - r * 0.7f),
            size = Size(r * 2f, r * 1.4f)
        )
    } else {
        // Organic realistic blood splatter pool
        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xCC330000), // dark clotted center
                    decal.color.copy(alpha = decal.alpha * 0.85f),
                    Color(0x00000000)
                ),
                center = Offset(dx, dy),
                radius = r
            ),
            topLeft = Offset(dx - r * 0.9f, dy - r * 0.6f),
            size = Size(r * 1.8f, r * 1.2f)
        )
        // Small satellite droplets
        drawCircle(color = decal.color.copy(alpha = decal.alpha * 0.7f), radius = r * 0.16f, center = Offset(dx + r * 0.85f, dy - r * 0.25f))
        drawCircle(color = decal.color.copy(alpha = decal.alpha * 0.7f), radius = r * 0.12f, center = Offset(dx - r * 0.75f, dy + r * 0.35f))
    }
}

/**
 * Renders a photorealistic, anatomically correct human zombie:
 * - Natural head, neck, broad shoulders, articulated arms, torso, legs with boots
 * - Real-world clothes: ripped blue denim jeans, work shirts, tattered hoodies, riot gear
 * - Mottled gray/olive decaying skin with bruises, necrotic veins and blood spatters
 * - Realistic sunken eye sockets with bioluminescent infected pupils that reflect light
 * - Fluid natural walking gait with shoulder sway and limb articulation
 */
private fun DrawScope.drawRealisticZombie(
    zombie: Zombie,
    width: Float,
    height: Float,
    shakeX: Float,
    shakeY: Float
) {
    val zx = zombie.x * width + shakeX
    val zy = zombie.y * height + shakeY
    val r = zombie.radius

    val isFlashing = zombie.hitFlashTimer > 0.05f
    val skinTone = if (isFlashing) Color(0xFFFFFFFF) else zombie.type.skinColor
    val clothesTone = if (isFlashing) Color(0xFFFF5252) else zombie.type.clothesColor

    // 1. Dual-layer Realistic Ambient Contact Shadow on Ground
    val shadowW = r * 2.2f
    val shadowH = r * 0.75f
    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(Color(0x99000000), Color(0x44000000), Color(0x00000000)),
            center = Offset(zx, zy + r * 0.72f),
            radius = shadowW / 2f
        ),
        topLeft = Offset(zx - shadowW / 2f, zy + r * 0.35f),
        size = Size(shadowW, shadowH)
    )

    // Boss special menacing biohazard aura
    if (zombie.type.isBoss) {
        val pulse = 1f + sin(System.currentTimeMillis() * 0.006f) * 0.08f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x55D50000), Color(0x22B71C1C), Color(0x00000000)),
                center = Offset(zx, zy),
                radius = r * 2.6f * pulse
            ),
            radius = r * 2.6f * pulse,
            center = Offset(zx, zy)
        )
    }

    // 2. Animated Legs with Real Pants, Knees & Combat Boots
    val legSwing = sin(zombie.walkAnim) * (r * 0.45f)

    // Left Leg (Denim pant leg with knee tear)
    drawLine(
        color = clothesTone,
        start = Offset(zx - r * 0.32f, zy + r * 0.35f),
        end = Offset(zx - r * 0.32f + legSwing, zy + r * 1.15f),
        strokeWidth = r * 0.34f
    )
    // Left Knee Flesh Tear
    drawCircle(
        color = Color(0xFF3E1C1C),
        radius = r * 0.10f,
        center = Offset(zx - r * 0.32f + legSwing * 0.5f, zy + r * 0.75f)
    )
    // Left Boot (Black combat boot with sole tread)
    drawRoundRect(
        color = Color(0xFF15191C),
        topLeft = Offset(zx - r * 0.44f + legSwing, zy + r * 1.10f),
        size = Size(r * 0.34f, r * 0.22f),
        cornerRadius = CornerRadius(2.5f, 2.5f)
    )
    drawLine(
        color = Color(0xFF2D3748),
        start = Offset(zx - r * 0.44f + legSwing, zy + r * 1.30f),
        end = Offset(zx - r * 0.10f + legSwing, zy + r * 1.30f),
        strokeWidth = 2f
    )

    // Right Leg (Denim pant leg)
    drawLine(
        color = clothesTone,
        start = Offset(zx + r * 0.32f, zy + r * 0.35f),
        end = Offset(zx + r * 0.32f - legSwing, zy + r * 1.15f),
        strokeWidth = r * 0.34f
    )
    // Right Boot
    drawRoundRect(
        color = Color(0xFF15191C),
        topLeft = Offset(zx + r * 0.20f - legSwing, zy + r * 1.10f),
        size = Size(r * 0.34f, r * 0.22f),
        cornerRadius = CornerRadius(2.5f, 2.5f)
    )
    drawLine(
        color = Color(0xFF2D3748),
        start = Offset(zx + r * 0.20f - legSwing, zy + r * 1.30f),
        end = Offset(zx + r * 0.54f - legSwing, zy + r * 1.30f),
        strokeWidth = 2f
    )

    // 3. Torso (Torn shirt / Tactical jacket with shading & texture)
    drawRoundRect(
        brush = Brush.verticalGradient(
            colors = listOf(clothesTone, Color(0xFF171A21)),
            startY = zy - r * 0.38f,
            endY = zy + r * 0.42f
        ),
        topLeft = Offset(zx - r * 0.65f, zy - r * 0.38f),
        size = Size(r * 1.3f, r * 0.88f),
        cornerRadius = CornerRadius(r * 0.25f, r * 0.25f)
    )

    // Ragged Bottom Hem of shirt
    for (i in 0 until 4) {
        val hemX = zx - r * 0.5f + i * (r * 0.32f)
        drawLine(
            color = Color(0xFF171A21),
            start = Offset(hemX, zy + r * 0.45f),
            end = Offset(hemX + 2f, zy + r * 0.52f),
            strokeWidth = 2.5f
        )
    }

    // Exposed Ribs / Gaping Flesh Wound with necrotic bone highlights
    drawOval(
        color = Color(0xFF380000),
        topLeft = Offset(zx - r * 0.24f, zy - r * 0.12f),
        size = Size(r * 0.48f, r * 0.38f)
    )
    // Broken Rib Bones
    drawLine(
        color = Color(0xFFE2E8F0),
        start = Offset(zx - r * 0.18f, zy - r * 0.04f),
        end = Offset(zx + r * 0.08f, zy - r * 0.04f),
        strokeWidth = 2f
    )
    drawLine(
        color = Color(0xFFE2E8F0),
        start = Offset(zx - r * 0.14f, zy + r * 0.06f),
        end = Offset(zx + r * 0.12f, zy + r * 0.06f),
        strokeWidth = 2f
    )

    // Brute Riot Armor Pauldrons & Tactical Chestplate
    if (zombie.type == ZombieType.BRUTE) {
        drawRoundRect(
            color = Color(0xFF263238),
            topLeft = Offset(zx - r * 0.95f, zy - r * 0.42f),
            size = Size(r * 0.48f, r * 0.52f),
            cornerRadius = CornerRadius(4f, 4f)
        )
        drawCircle(color = Color(0xFF78909C), radius = 2.5f, center = Offset(zx - r * 0.72f, zy - r * 0.25f))

        drawRoundRect(
            color = Color(0xFF263238),
            topLeft = Offset(zx + r * 0.47f, zy - r * 0.42f),
            size = Size(r * 0.48f, r * 0.52f),
            cornerRadius = CornerRadius(4f, 4f)
        )
        drawCircle(color = Color(0xFF78909C), radius = 2.5f, center = Offset(zx + r * 0.72f, zy - r * 0.25f))
    }

    // Toxic Pustules glowing with radioactive green light
    if (zombie.type == ZombieType.TOXIC) {
        val pulseToxic = 1f + sin(System.currentTimeMillis() * 0.01f + zombie.id) * 0.15f
        drawCircle(
            color = Color(0x8876FF03),
            radius = r * 0.22f * pulseToxic,
            center = Offset(zx - r * 0.40f, zy - r * 0.15f)
        )
        drawCircle(
            color = Color(0xFF00E676),
            radius = r * 0.12f,
            center = Offset(zx - r * 0.40f, zy - r * 0.15f)
        )
    }

    // 4. Arms with Articulated Forearms and Reaching Clawed Hands
    val armSwing = sin(zombie.walkAnim * 1.2f) * (r * 0.22f)
    // Left Arm
    drawLine(
        color = skinTone,
        start = Offset(zx - r * 0.65f, zy - r * 0.18f),
        end = Offset(zx - r * 0.80f, zy + r * 0.68f + armSwing),
        strokeWidth = r * 0.26f
    )
    // Left Clawed Hand & Fingers
    drawCircle(color = Color(0xFF171B19), radius = r * 0.13f, center = Offset(zx - r * 0.80f, zy + r * 0.68f + armSwing))
    drawLine(
        color = Color(0xFF101412),
        start = Offset(zx - r * 0.80f, zy + r * 0.68f + armSwing),
        end = Offset(zx - r * 0.86f, zy + r * 0.84f + armSwing),
        strokeWidth = 2f
    )

    // Right Arm
    drawLine(
        color = skinTone,
        start = Offset(zx + r * 0.65f, zy - r * 0.18f),
        end = Offset(zx + r * 0.80f, zy + r * 0.68f - armSwing),
        strokeWidth = r * 0.26f
    )
    // Right Clawed Hand & Fingers
    drawCircle(color = Color(0xFF171B19), radius = r * 0.13f, center = Offset(zx + r * 0.80f, zy + r * 0.68f - armSwing))
    drawLine(
        color = Color(0xFF101412),
        start = Offset(zx + r * 0.80f, zy + r * 0.68f - armSwing),
        end = Offset(zx + r * 0.86f, zy + r * 0.84f - armSwing),
        strokeWidth = 2f
    )

    // 5. Realistic Human Head with Rotting Cranium & Jaw Structure
    // Neck
    drawRect(
        color = skinTone.copy(alpha = 0.9f),
        topLeft = Offset(zx - r * 0.2f, zy - r * 0.42f),
        size = Size(r * 0.4f, r * 0.2f)
    )

    // Cranium with Anatomical Shading
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(skinTone, Color(0xFF141E12)),
            center = Offset(zx, zy - r * 0.64f),
            radius = r * 0.64f
        ),
        radius = r * 0.60f,
        center = Offset(zx, zy - r * 0.64f)
    )

    // Sunken Eye Sockets (Dark shadowy cavities)
    val eyeY = zy - r * 0.66f
    drawCircle(color = Color(0xFF0A0D0B), radius = r * 0.18f, center = Offset(zx - r * 0.22f, eyeY))
    drawCircle(color = Color(0xFF0A0D0B), radius = r * 0.18f, center = Offset(zx + r * 0.22f, eyeY))

    // Glowing Bioluminescent Infected Irises with Specular Light Reflection
    val eyeColor = if (zombie.isFrozen) Color(0xFF00E5FF) else zombie.type.eyeColor
    // Left Eye
    drawCircle(color = eyeColor.copy(alpha = 0.5f), radius = r * 0.19f, center = Offset(zx - r * 0.22f, eyeY))
    drawCircle(color = eyeColor, radius = r * 0.11f, center = Offset(zx - r * 0.22f, eyeY))
    drawCircle(color = Color.White, radius = 1.6f, center = Offset(zx - r * 0.24f, eyeY - 1.5f))

    // Right Eye
    drawCircle(color = eyeColor.copy(alpha = 0.5f), radius = r * 0.19f, center = Offset(zx + r * 0.22f, eyeY))
    drawCircle(color = eyeColor, radius = r * 0.11f, center = Offset(zx + r * 0.22f, eyeY))
    drawCircle(color = Color.White, radius = 1.6f, center = Offset(zx + r * 0.20f, eyeY - 1.5f))

    // Gnashing Jaw & Teeth
    drawLine(
        color = Color(0xFF141414),
        start = Offset(zx - r * 0.22f, zy - r * 0.40f),
        end = Offset(zx + r * 0.22f, zy - r * 0.40f),
        strokeWidth = 3.5f
    )
    drawCircle(color = Color(0xFFF1F5F9), radius = 2f, center = Offset(zx - r * 0.12f, zy - r * 0.40f))
    drawCircle(color = Color(0xFFF1F5F9), radius = 2f, center = Offset(zx + r * 0.12f, zy - r * 0.40f))
    drawCircle(color = Color(0xFFB91C1C), radius = 1.4f, center = Offset(zx, zy - r * 0.38f))

    // Cryo Ice Overlay when frozen
    if (zombie.isFrozen) {
        drawCircle(color = Color(0x6600E5FF), radius = r * 1.25f, center = Offset(zx, zy))
        drawCircle(color = Color(0xCC80D8FF), radius = r * 1.25f, center = Offset(zx, zy), style = Stroke(width = 3f))
    }

    // Modern Segmented Health Bar
    if (zombie.currentHp < zombie.maxHp || zombie.type.isBoss) {
        val barWidth = r * 2.4f
        val barHeight = if (zombie.type.isBoss) 9f else 5f
        val barTop = zy - r * 1.28f
        val hpRatio = (zombie.currentHp / zombie.maxHp).coerceIn(0f, 1f)

        // Backdrop
        drawRoundRect(
            color = Color(0xDD090D12),
            topLeft = Offset(zx - barWidth / 2f - 2f, barTop - 2f),
            size = Size(barWidth + 4f, barHeight + 4f),
            cornerRadius = CornerRadius(2f, 2f)
        )
        val gaugeColor = when {
            hpRatio > 0.6f -> Color(0xFF00E676)
            hpRatio > 0.3f -> Color(0xFFFFD600)
            else -> Color(0xFFFF1744)
        }
        drawRoundRect(
            color = gaugeColor,
            topLeft = Offset(zx - barWidth / 2f, barTop),
            size = Size(barWidth * hpRatio, barHeight),
            cornerRadius = CornerRadius(2f, 2f)
        )
    }
}

/**
 * Atmospheric soft cinematic vignette on viewport borders
 */
private fun DrawScope.drawAmbientAtmosphericVignette(width: Float, height: Float) {
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.Transparent,
                Color(0x00000000),
                Color(0x33000000),
                Color(0x9905070B)
            ),
            center = Offset(width * 0.5f, height * 0.5f),
            radius = (width.coerceAtLeast(height)) * 0.72f
        ),
        size = Size(width, height)
    )
}

/**
 * Smooth atmospheric ash and dust embers drifting across the city screen
 */
private fun DrawScope.drawSoftAtmosphericEmbers(width: Float, height: Float) {
    val time = System.currentTimeMillis() * 0.001f
    for (i in 0 until 14) {
        val seed = i * 47.3f
        val x = ((sin(time * 0.3f + seed) * 0.45f + 0.5f) * width).coerceIn(0f, width)
        val y = (((time * 0.08f * (1f + (i % 3) * 0.4f) + seed * 0.1f) % 1f) * height)
        val size = 2f + (i % 3) * 1.5f
        val alpha = (0.25f + sin(time * 2f + seed) * 0.2f).coerceIn(0.1f, 0.6f)
        val color = if (i % 2 == 0) Color(0xFFFFB74D) else Color(0xFF90CAF9)
        drawCircle(
            color = color.copy(alpha = alpha),
            radius = size,
            center = Offset(x, y)
        )
    }
}

/**
 * Ballistic Projectiles with Smooth Glow
 */
private fun DrawScope.drawBallisticProjectile(
    p: Projectile,
    width: Float,
    height: Float,
    shakeX: Float,
    shakeY: Float
) {
    val px = p.x * width + shakeX
    val py = p.y * height + shakeY

    if (p.isExplosive) {
        val missileLength = 22f
        val normV = sqrt(p.vx * p.vx + p.vy * p.vy).coerceAtLeast(0.001f)
        val dirX = p.vx / normV
        val dirY = p.vy / normV

        // Rocket body
        drawLine(
            color = Color(0xFF558B2F),
            start = Offset(px - dirX * missileLength, py - dirY * missileLength),
            end = Offset(px, py),
            strokeWidth = 6f
        )
        drawCircle(color = Color(0xFFFF1744), radius = 5f, center = Offset(px, py))
        drawCircle(color = Color(0xFFFF9100), radius = 4f, center = Offset(px - dirX * (missileLength + 3f), py - dirY * (missileLength + 3f)))
    } else {
        val tracerLength = 24f
        val normV = sqrt(p.vx * p.vx + p.vy * p.vy).coerceAtLeast(0.001f)
        val dirX = p.vx / normV
        val dirY = p.vy / normV

        // Soft Outer Glow
        drawLine(
            color = p.color.copy(alpha = 0.5f),
            start = Offset(px - dirX * tracerLength, py - dirY * tracerLength),
            end = Offset(px, py),
            strokeWidth = if (p.remainingPierce > 1) 6f else 4f
        )
        // White Core
        drawLine(
            color = Color.White,
            start = Offset(px - dirX * (tracerLength * 0.6f), py - dirY * (tracerLength * 0.6f)),
            end = Offset(px, py),
            strokeWidth = 2f
        )
    }
}

/**
 * Realistic Brass Shell Casing with 3D rotation and metallic sheen
 */
private fun DrawScope.drawRealisticShellCasing(
    shell: ShellCasing,
    width: Float,
    height: Float,
    shakeX: Float,
    shakeY: Float
) {
    val sx = shell.x * width + shakeX
    val sy = shell.y * height + shakeY

    rotate(degrees = shell.rotation, pivot = Offset(sx, sy)) {
        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFFFFEE58), Color(0xFFF57F17)),
                start = Offset(sx - 2.5f, sy),
                end = Offset(sx + 2.5f, sy)
            ),
            topLeft = Offset(sx - 2.5f, sy - 5.5f),
            size = Size(5f, 11f),
            cornerRadius = CornerRadius(1.5f, 1.5f)
        )
    }
}

/**
 * Modern Military Holographic HUD Reticle & Laser Sighting
 */
private fun DrawScope.drawModernHolographicReticle(
    aimNormX: Float,
    aimNormY: Float,
    soldierNormX: Float,
    width: Float,
    height: Float,
    barricadeY: Float,
    shakeX: Float,
    shakeY: Float
) {
    val ax = aimNormX * width + shakeX
    val ay = aimNormY * height + shakeY
    val soldierX = soldierNormX * width + shakeX
    val soldierY = barricadeY + 24f + shakeY

    // 1. Collimated Laser Sighting Line from Soldier's Weapon
    drawLine(
        brush = Brush.linearGradient(
            colors = listOf(Color(0x33FF1744), Color(0xDDFF1744)),
            start = Offset(soldierX, soldierY),
            end = Offset(ax, ay)
        ),
        start = Offset(soldierX, soldierY),
        end = Offset(ax, ay),
        strokeWidth = 1.6f
    )

    // 2. Holographic Target Reticle
    val reticleRadius = 26f
    val bracketLen = 8f

    // 4 Corner Brackets
    // Top-Left
    drawLine(color = Color(0xEE00E676), start = Offset(ax - reticleRadius, ay - reticleRadius + bracketLen), end = Offset(ax - reticleRadius, ay - reticleRadius), strokeWidth = 2f)
    drawLine(color = Color(0xEE00E676), start = Offset(ax - reticleRadius, ay - reticleRadius), end = Offset(ax - reticleRadius + bracketLen, ay - reticleRadius), strokeWidth = 2f)
    // Top-Right
    drawLine(color = Color(0xEE00E676), start = Offset(ax + reticleRadius - bracketLen, ay - reticleRadius), end = Offset(ax + reticleRadius, ay - reticleRadius), strokeWidth = 2f)
    drawLine(color = Color(0xEE00E676), start = Offset(ax + reticleRadius, ay - reticleRadius), end = Offset(ax + reticleRadius, ay - reticleRadius + bracketLen), strokeWidth = 2f)
    // Bottom-Left
    drawLine(color = Color(0xEE00E676), start = Offset(ax - reticleRadius, ay + reticleRadius - bracketLen), end = Offset(ax - reticleRadius, ay + reticleRadius), strokeWidth = 2f)
    drawLine(color = Color(0xEE00E676), start = Offset(ax - reticleRadius, ay + reticleRadius), end = Offset(ax - reticleRadius + bracketLen, ay + reticleRadius), strokeWidth = 2f)
    // Bottom-Right
    drawLine(color = Color(0xEE00E676), start = Offset(ax + reticleRadius - bracketLen, ay + reticleRadius), end = Offset(ax + reticleRadius, ay + reticleRadius), strokeWidth = 2f)
    drawLine(color = Color(0xEE00E676), start = Offset(ax + reticleRadius, ay + reticleRadius), end = Offset(ax + reticleRadius, ay + reticleRadius - bracketLen), strokeWidth = 2f)

    // Center Crosshair
    drawCircle(color = Color(0xFFFF1744), radius = 3.5f, center = Offset(ax, ay))
    drawCircle(color = Color(0x7700E676), radius = reticleRadius * 0.7f, center = Offset(ax, ay), style = Stroke(width = 1.2f))
}

/**
 * Heavy Ballistic Barricade: Sandbags, Steel Armor, Razor Wire & Living Animated Soldier
 */
private fun DrawScope.drawRealisticMilitaryBarricade(
    width: Float,
    height: Float,
    barricadeY: Float,
    currentHp: Float,
    maxHp: Float,
    flashTimer: Float,
    spikesLevel: Int,
    muzzleFlashTimer: Float,
    aimNormX: Float,
    aimNormY: Float,
    soldierNormX: Float,
    soldierWalkAnim: Float,
    weaponType: WeaponType,
    shakeX: Float,
    shakeY: Float
) {
    val barricadeHeight = height - barricadeY

    // 1. Muzzle Flash Volumetric Warm Light from Soldier Position
    val soldierX = soldierNormX * width + shakeX
    val soldierY = barricadeY + 26f + shakeY

    if (muzzleFlashTimer > 0.05f) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0x99FFE082).copy(alpha = muzzleFlashTimer * 0.8f),
                    Color(0x33FFB300).copy(alpha = muzzleFlashTimer * 0.4f),
                    Color(0x00000000)
                ),
                center = Offset(soldierX, soldierY),
                radius = width * 0.75f
            ),
            radius = width * 0.75f,
            center = Offset(soldierX, soldierY)
        )
    }

    // 2. Heavy Composite Armor Steel Wall
    val wallColor = if (flashTimer > 0.05f) Color(0xFFC62828) else Color(0xFF161F26)
    drawRect(
        color = wallColor,
        topLeft = Offset(0f + shakeX, barricadeY + shakeY),
        size = Size(width, barricadeHeight)
    )

    // Riveted Metal Bolts along armor seams
    val rivetCount = (width / 38f).toInt()
    for (i in 0..rivetCount) {
        val rx = i * 38f + 10f + shakeX
        drawCircle(color = Color(0xFF37474F), radius = 3f, center = Offset(rx, barricadeY + 10f + shakeY))
        drawCircle(color = Color(0xFFB0BEC5), radius = 1.5f, center = Offset(rx - 0.5f, barricadeY + 9.5f + shakeY))
    }

    // 3. Sandbags stacked in base layer
    val sandbagW = 34f
    val sandbagH = 14f
    var sbX = 0f + shakeX
    while (sbX < width) {
        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF5D4037), Color(0xFF3E2723)),
                startY = barricadeY + 14f,
                endY = barricadeY + 28f
            ),
            topLeft = Offset(sbX, barricadeY + 14f + shakeY),
            size = Size(sandbagW, sandbagH),
            cornerRadius = CornerRadius(4f, 4f)
        )
        sbX += sandbagW + 2f
    }

    // 4. Bold Hazard Caution Stripes
    val stripeWidth = 26f
    var sx = -stripeWidth + shakeX
    while (sx < width + stripeWidth) {
        val path = Path().apply {
            moveTo(sx, barricadeY + 28f + shakeY)
            lineTo(sx + stripeWidth * 0.6f, barricadeY + 28f + shakeY)
            lineTo(sx + stripeWidth * 1.6f, barricadeY + 42f + shakeY)
            lineTo(sx + stripeWidth, barricadeY + 42f + shakeY)
            close()
        }
        drawPath(path, color = Color(0xFFFFD600), style = Fill)
        sx += stripeWidth * 2
    }

    // 5. Razor Sharp Barbed Wire along top rail
    var wireX = 0f + shakeX
    while (wireX < width + 20f) {
        drawOval(
            color = Color(0xFFCFD8DC),
            topLeft = Offset(wireX, barricadeY - 8f + shakeY),
            size = Size(22f, 16f),
            style = Stroke(width = 2f)
        )
        wireX += 18f
    }

    // 6. Electrified Defensive Spikes (Tesla Field)
    if (spikesLevel > 0) {
        val spikeCount = (width / 30f).toInt()
        val spikeStep = width / spikeCount
        for (i in 0 until spikeCount) {
            val spX = i * spikeStep + spikeStep / 2f + shakeX
            val spTop = barricadeY - 18f - (spikesLevel * 3f) + shakeY
            val path = Path().apply {
                moveTo(spX - 9f, barricadeY + shakeY)
                lineTo(spX, spTop)
                lineTo(spX + 9f, barricadeY + shakeY)
                close()
            }
            drawPath(path, color = Color(0xFF78909C))
            drawPath(path, color = Color(0xFF00E5FF), style = Stroke(width = 2f))
        }
    }

    // 7. REAL LIVING SOLDIER walking, aiming, and firing according to position!
    val aimAngle = atan2(aimNormY * height - soldierY, aimNormX * width - soldierX)
    drawRealisticSoldier(
        soldierX = soldierX,
        soldierY = soldierY,
        aimAngle = aimAngle,
        walkAnim = soldierWalkAnim,
        muzzleFlashTimer = muzzleFlashTimer,
        weaponType = weaponType
    )

    // 8. Barricade Shield Gauge (Modern Segmented Tech HUD)
    val hpRatio = (currentHp / maxHp).coerceIn(0f, 1f)
    val gaugeWidth = width * 0.72f
    val gaugeX = (width - gaugeWidth) / 2f + shakeX
    val gaugeY = barricadeY + 54f + shakeY

    drawRoundRect(
        color = Color(0xEE090D11),
        topLeft = Offset(gaugeX, gaugeY),
        size = Size(gaugeWidth, 8f),
        cornerRadius = CornerRadius(4f, 4f)
    )
    val barColor = when {
        hpRatio > 0.5f -> Color(0xFF00E676)
        hpRatio > 0.25f -> Color(0xFFFFB300)
        else -> Color(0xFFFF1744)
    }
    drawRoundRect(
        color = barColor,
        topLeft = Offset(gaugeX, gaugeY),
        size = Size(gaugeWidth * hpRatio, 8f),
        cornerRadius = CornerRadius(4f, 4f)
    )
}

/**
 * Photorealistic Articulated Tactical Soldier:
 * - Walking leg stride animation with articulated knees, lift & heavy combat boots
 * - Digital camouflage uniform, tactical MOLLE ballistic plate vest, chest pouches, radio antenna
 * - Dynamic aim angle tracking with custom weapon rendering (Pistol, Shotgun, M4, Sniper, RPG, Plasma)
 * - Recoil kickback on fire pushing torso and arms backward
 * - FAST high-cut ballistic helmet with glowing Night-Vision optic visor
 * - Volumetric starburst muzzle flash & chamber ejection
 */
private fun DrawScope.drawRealisticSoldier(
    soldierX: Float,
    soldierY: Float,
    aimAngle: Float,
    walkAnim: Float,
    muzzleFlashTimer: Float,
    weaponType: WeaponType
) {
    // 1. Soft Ground Platform Shadow
    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xBB000000), Color(0x00000000)),
            center = Offset(soldierX, soldierY + 18f),
            radius = 28f
        ),
        topLeft = Offset(soldierX - 24f, soldierY + 6f),
        size = Size(48f, 24f)
    )

    // 2. Animated Legs with Natural Walking Stride & Knee Articulation
    val legSwing = sin(walkAnim) * 14f
    val leftLift = if (legSwing > 0f) -sin(walkAnim) * 5f else 0f
    val rightLift = if (legSwing < 0f) sin(walkAnim) * 5f else 0f
    val strideBobY = sin(walkAnim * 2f) * 1.5f

    // Left Leg (Camo trousers with knee bending)
    val lHipX = soldierX - 8f
    val lHipY = soldierY + 2f + strideBobY
    val lKneeX = lHipX + legSwing * 0.45f
    val lKneeY = soldierY + 12f + leftLift + strideBobY
    val lFootX = lHipX + legSwing
    val lFootY = soldierY + 22f + leftLift + strideBobY

    drawLine(color = Color(0xFF2E3D29), start = Offset(lHipX, lHipY), end = Offset(lKneeX, lKneeY), strokeWidth = 9.5f)
    drawLine(color = Color(0xFF273623), start = Offset(lKneeX, lKneeY), end = Offset(lFootX, lFootY), strokeWidth = 8.5f)
    // Left Combat Boot with tread
    drawRoundRect(
        color = Color(0xFF141414),
        topLeft = Offset(lFootX - 5f, lFootY - 3f),
        size = Size(12f, 8f),
        cornerRadius = CornerRadius(2.5f, 2.5f)
    )
    drawRoundRect(
        color = Color(0xFF2E3D29),
        topLeft = Offset(lKneeX - 3.5f, lKneeY - 3.5f),
        size = Size(7f, 7f),
        cornerRadius = CornerRadius(2f, 2f)
    )

    // Right Leg
    val rHipX = soldierX + 8f
    val rHipY = soldierY + 2f + strideBobY
    val rKneeX = rHipX - legSwing * 0.45f
    val rKneeY = soldierY + 12f + rightLift + strideBobY
    val rFootX = rHipX - legSwing
    val rFootY = soldierY + 22f + rightLift + strideBobY

    drawLine(color = Color(0xFF2E3D29), start = Offset(rHipX, rHipY), end = Offset(rKneeX, rKneeY), strokeWidth = 9.5f)
    drawLine(color = Color(0xFF273623), start = Offset(rKneeX, rKneeY), end = Offset(rFootX, rFootY), strokeWidth = 8.5f)
    // Right Combat Boot
    drawRoundRect(
        color = Color(0xFF141414),
        topLeft = Offset(rFootX - 5f, rFootY - 3f),
        size = Size(12f, 8f),
        cornerRadius = CornerRadius(2.5f, 2.5f)
    )
    drawRoundRect(
        color = Color(0xFF2E3D29),
        topLeft = Offset(rKneeX - 3.5f, rKneeY - 3.5f),
        size = Size(7f, 7f),
        cornerRadius = CornerRadius(2f, 2f)
    )

    // 3. Torso with Tactical Camo Uniform & Heavy Ballistic Plate Vest
    // Dynamic Recoil Kickback along aim angle when firing
    val recoilDist = if (muzzleFlashTimer > 0f) -7f * muzzleFlashTimer else 0f
    val recoilX = cos(aimAngle) * recoilDist
    val recoilY = sin(aimAngle) * recoilDist
    val torsoX = soldierX + recoilX
    val torsoY = soldierY + recoilY + strideBobY

    // Tactical Camo Fatigue Shirt
    drawRoundRect(
        color = Color(0xFF33452E),
        topLeft = Offset(torsoX - 16f, torsoY - 14f),
        size = Size(32f, 22f),
        cornerRadius = CornerRadius(4.5f, 4.5f)
    )
    // Ballistic Armor Plate Carrier (MOLLE webbing)
    drawRoundRect(
        color = Color(0xFF1B2418),
        topLeft = Offset(torsoX - 13f, torsoY - 12f),
        size = Size(26f, 19f),
        cornerRadius = CornerRadius(3.5f, 3.5f)
    )
    // 3 Ammo Magazine Pouches across chest
    for (i in 0..2) {
        val px = torsoX - 10f + i * 7.5f
        drawRoundRect(
            color = Color(0xFF263321),
            topLeft = Offset(px, torsoY - 2f),
            size = Size(6.5f, 8.5f),
            cornerRadius = CornerRadius(1.5f, 1.5f)
        )
    }
    // Radio Antenna with blinking green LED comms indicator
    drawLine(
        color = Color(0xFF111111),
        start = Offset(torsoX - 10f, torsoY - 12f),
        end = Offset(torsoX - 13f, torsoY - 28f),
        strokeWidth = 2f
    )
    drawCircle(
        color = Color(0xFF00E676),
        radius = 1.8f,
        center = Offset(torsoX - 13f, torsoY - 28f)
    )

    // 4. Arms & Weapon according to weaponType and aimAngle
    val gunStartX = torsoX
    val gunStartY = torsoY - 2f

    val barrelLen = when (weaponType) {
        WeaponType.PISTOL -> 22f
        WeaponType.SHOTGUN -> 36f
        WeaponType.ASSAULT_RIFLE -> 38f
        WeaponType.SNIPER -> 50f
        WeaponType.GRENADE_LAUNCHER -> 42f
        WeaponType.PLASMA_CANNON -> 40f
    }

    val gunEndX = gunStartX + cos(aimAngle) * barrelLen
    val gunEndY = gunStartY + sin(aimAngle) * barrelLen

    // Left Arm (Foregrip support)
    val leftArmReach = when (weaponType) {
        WeaponType.PISTOL -> 14f
        WeaponType.SNIPER, WeaponType.SHOTGUN -> 22f
        WeaponType.GRENADE_LAUNCHER -> 18f
        else -> 16f
    }
    drawLine(
        color = Color(0xFF33452E),
        start = Offset(torsoX - 12f, torsoY - 6f),
        end = Offset(gunStartX + cos(aimAngle) * leftArmReach, gunStartY + sin(aimAngle) * leftArmReach),
        strokeWidth = 6.5f
    )
    // Right Arm (Pistol grip & trigger)
    drawLine(
        color = Color(0xFF33452E),
        start = Offset(torsoX + 12f, torsoY - 6f),
        end = Offset(gunStartX + cos(aimAngle) * 16f, gunStartY + sin(aimAngle) * 16f),
        strokeWidth = 6.5f
    )
    // Tactical Combat Gloves
    drawCircle(color = Color(0xFF1B1B1B), radius = 3.5f, center = Offset(gunStartX + cos(aimAngle) * leftArmReach, gunStartY + sin(aimAngle) * leftArmReach))
    drawCircle(color = Color(0xFF1B1B1B), radius = 3.5f, center = Offset(gunStartX + cos(aimAngle) * 16f, gunStartY + sin(aimAngle) * 16f))

    // RENDER DETAILED FIREARM ACCORDING TO WEAPON TYPE
    when (weaponType) {
        WeaponType.PISTOL -> {
            drawLine(
                color = Color(0xFF212121),
                start = Offset(gunStartX, gunStartY),
                end = Offset(gunEndX, gunEndY),
                strokeWidth = 5.5f
            )
            drawLine(
                color = Color(0xFF424242),
                start = Offset(gunStartX, gunStartY),
                end = Offset(gunStartX + cos(aimAngle) * 12f, gunStartY + sin(aimAngle) * 12f),
                strokeWidth = 6.5f
            )
            drawCircle(
                color = Color(0xFFFF1744),
                radius = 1.5f,
                center = Offset(gunStartX + cos(aimAngle) * 16f + sin(aimAngle) * 3f, gunStartY + sin(aimAngle) * 16f - cos(aimAngle) * 3f)
            )
        }
        WeaponType.SHOTGUN -> {
            drawLine(
                color = Color(0xFF263238),
                start = Offset(gunStartX, gunStartY),
                end = Offset(gunEndX, gunEndY),
                strokeWidth = 6.5f
            )
            drawLine(
                color = Color(0xFF1A1A1A),
                start = Offset(gunStartX + sin(aimAngle) * 3f, gunStartY - cos(aimAngle) * 3f),
                end = Offset(gunEndX + sin(aimAngle) * 3f, gunEndY - cos(aimAngle) * 3f),
                strokeWidth = 4f
            )
            val pumpStart = gunStartX + cos(aimAngle) * 15f
            val pumpStartY = gunStartY + sin(aimAngle) * 15f
            val pumpEnd = gunStartX + cos(aimAngle) * 25f
            val pumpEndY = gunStartY + sin(aimAngle) * 25f
            drawLine(color = Color(0xFF5D4037), start = Offset(pumpStart, pumpStartY), end = Offset(pumpEnd, pumpEndY), strokeWidth = 8f)
        }
        WeaponType.ASSAULT_RIFLE -> {
            drawLine(
                color = Color(0xFF1E262B),
                start = Offset(gunStartX, gunStartY),
                end = Offset(gunEndX, gunEndY),
                strokeWidth = 5.5f
            )
            val magX = gunStartX + cos(aimAngle) * 12f
            val magY = gunStartY + sin(aimAngle) * 12f
            drawLine(
                color = Color(0xFF37474F),
                start = Offset(magX, magY),
                end = Offset(magX + sin(aimAngle) * 10f, magY - cos(aimAngle) * 10f),
                strokeWidth = 4f
            )
            val sightX = gunStartX + cos(aimAngle) * 8f - sin(aimAngle) * 4f
            val sightY = gunStartY + sin(aimAngle) * 8f + cos(aimAngle) * 4f
            drawRoundRect(
                color = Color(0xFF15191C),
                topLeft = Offset(sightX - 4f, sightY - 3f),
                size = Size(8f, 6f),
                cornerRadius = CornerRadius(1.5f, 1.5f)
            )
            drawCircle(color = Color(0xFFFF1744), radius = 1.2f, center = Offset(sightX, sightY))
        }
        WeaponType.SNIPER -> {
            drawLine(
                color = Color(0xFF1B2418),
                start = Offset(gunStartX, gunStartY),
                end = Offset(gunEndX, gunEndY),
                strokeWidth = 6.5f
            )
            drawLine(
                color = Color(0xFF263238),
                start = Offset(gunStartX + cos(aimAngle) * 15f, gunStartY + sin(aimAngle) * 15f),
                end = Offset(gunEndX, gunEndY),
                strokeWidth = 4.5f
            )
            drawRoundRect(
                color = Color(0xFF0D1117),
                topLeft = Offset(gunEndX - 3f, gunEndY - 3f),
                size = Size(6f, 6f),
                cornerRadius = CornerRadius(1f, 1f)
            )
            val scopeX = gunStartX + cos(aimAngle) * 12f - sin(aimAngle) * 5f
            val scopeY = gunStartY + sin(aimAngle) * 12f + cos(aimAngle) * 5f
            drawLine(
                color = Color(0xFF111111),
                start = Offset(scopeX - cos(aimAngle) * 9f, scopeY - sin(aimAngle) * 9f),
                end = Offset(scopeX + cos(aimAngle) * 11f, scopeY + sin(aimAngle) * 11f),
                strokeWidth = 5f
            )
            drawCircle(color = Color(0xFF00E676), radius = 1.6f, center = Offset(scopeX + cos(aimAngle) * 11f, scopeY + sin(aimAngle) * 11f))
            val bipodX = gunStartX + cos(aimAngle) * 32f
            val bipodY = gunStartY + sin(aimAngle) * 32f
            drawLine(color = Color(0xFF455A64), start = Offset(bipodX, bipodY), end = Offset(bipodX + sin(aimAngle) * 6f, bipodY - cos(aimAngle) * 6f), strokeWidth = 2.5f)
        }
        WeaponType.GRENADE_LAUNCHER -> {
            val launcherWidth = 10f
            drawLine(
                color = Color(0xFF33452E),
                start = Offset(gunStartX - cos(aimAngle) * 10f, gunStartY - sin(aimAngle) * 10f),
                end = Offset(gunEndX, gunEndY),
                strokeWidth = launcherWidth
            )
            val warheadX = gunEndX + cos(aimAngle) * 6f
            val warheadY = gunEndY + sin(aimAngle) * 6f
            drawCircle(color = Color(0xFF558B2F), radius = 5.5f, center = Offset(warheadX, warheadY))
            drawCircle(color = Color(0xFFFFD54F), radius = 2f, center = Offset(warheadX + cos(aimAngle) * 4f, warheadY + sin(aimAngle) * 4f))
            drawCircle(color = Color(0xFF212121), radius = 4f, center = Offset(gunStartX - cos(aimAngle) * 10f, gunStartY - sin(aimAngle) * 10f))
        }
        WeaponType.PLASMA_CANNON -> {
            drawLine(
                color = Color(0xFF0D1B2A),
                start = Offset(gunStartX, gunStartY),
                end = Offset(gunEndX, gunEndY),
                strokeWidth = 8f
            )
            for (i in 1..3) {
                val coilX = gunStartX + cos(aimAngle) * (i * 9f + 6f)
                val coilY = gunStartY + sin(aimAngle) * (i * 9f + 6f)
                drawCircle(color = Color(0xFF00E5FF), radius = 5f, center = Offset(coilX, coilY))
                drawCircle(color = Color(0xFFE0F7FA), radius = 2.5f, center = Offset(coilX, coilY))
            }
            drawCircle(color = Color(0xFF00E5FF), radius = 5.5f, center = Offset(gunEndX, gunEndY), style = Stroke(width = 2f))
        }
    }

    // 5. Head with Tactical Ballistic Helmet & NVG Optic
    drawCircle(
        color = Color(0xFF273623),
        radius = 11.5f,
        center = Offset(torsoX, torsoY - 16f)
    )
    drawLine(color = Color(0xFF1B1B1B), start = Offset(torsoX - 10f, torsoY - 15f), end = Offset(torsoX - 4f, torsoY - 8f), strokeWidth = 1.5f)
    drawLine(color = Color(0xFF1B1B1B), start = Offset(torsoX + 10f, torsoY - 15f), end = Offset(torsoX + 4f, torsoY - 8f), strokeWidth = 1.5f)

    // Face Balaclava
    drawRoundRect(
        color = Color(0xFF151515),
        topLeft = Offset(torsoX - 7f, torsoY - 15f),
        size = Size(14f, 10f),
        cornerRadius = CornerRadius(2.5f, 2.5f)
    )
    // Night Vision Goggles / Glowing Tactical Visor with glow halo
    val eyeOffsetX = cos(aimAngle) * 2.2f
    val eyeOffsetY = sin(aimAngle) * 2.2f
    drawRoundRect(
        color = Color(0xFF00E676).copy(alpha = 0.4f),
        topLeft = Offset(torsoX - 8f + eyeOffsetX, torsoY - 16f + eyeOffsetY),
        size = Size(16f, 6f),
        cornerRadius = CornerRadius(3f, 3f)
    )
    drawRoundRect(
        color = Color(0xFF00E676),
        topLeft = Offset(torsoX - 6f + eyeOffsetX, torsoY - 15f + eyeOffsetY),
        size = Size(12f, 3.5f),
        cornerRadius = CornerRadius(1.5f, 1.5f)
    )

    // 6. Volumetric Muzzle Flash Starburst & Heat Flare from Weapon Tip
    if (muzzleFlashTimer > 0.05f) {
        val flashRadius = when (weaponType) {
            WeaponType.PISTOL -> 18f
            WeaponType.SHOTGUN -> 32f
            WeaponType.ASSAULT_RIFLE -> 24f
            WeaponType.SNIPER -> 38f
            WeaponType.GRENADE_LAUNCHER -> 42f
            WeaponType.PLASMA_CANNON -> 36f
        }

        val flashColors = when (weaponType) {
            WeaponType.PLASMA_CANNON -> listOf(Color(0xFFE0F7FA), Color(0xFF00E5FF), Color(0x0000B0FF))
            WeaponType.GRENADE_LAUNCHER -> listOf(Color(0xFFFFEB3B), Color(0xFFFF5722), Color(0x00D50000))
            else -> listOf(Color(0xFFFFF9C4), Color(0xFFFFB300), Color(0x00FF8F00))
        }

        drawCircle(
            brush = Brush.radialGradient(
                colors = flashColors,
                center = Offset(gunEndX, gunEndY),
                radius = flashRadius
            ),
            radius = flashRadius,
            center = Offset(gunEndX, gunEndY)
        )

        for (i in 0..3) {
            val angle = aimAngle + (i * 45f) * (Math.PI.toFloat() / 180f)
            val spireLen = flashRadius * 1.25f
            drawLine(
                color = flashColors[0],
                start = Offset(gunEndX, gunEndY),
                end = Offset(gunEndX + cos(angle) * spireLen, gunEndY + sin(angle) * spireLen),
                strokeWidth = 2.2f
            )
        }
    }
}
