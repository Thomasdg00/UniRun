# Font Installation Instructions

To complete the Kinetic design system implementation, you need to download the following Google Fonts and place them in the `app/src/main/res/font/` directory:

## Required Fonts

1. **Lexend** (for headlines - bold/italic)
   - Download from: https://fonts.google.com/specimen/Lexend
   - Weight: Bold (700)
   - File to place: `app/src/main/res/font/lexend.ttf`

2. **Manrope** (for body text)
   - Download from: https://fonts.google.com/specimen/Manrope
   - Weight: Regular (400)
   - File to place: `app/src/main/res/font/manrope.ttf`

3. **Space Grotesk** (for labels and UI text)
   - Download from: https://fonts.google.com/specimen/Space+Grotesk
   - Weight: Medium (500)
   - File to place: `app/src/main/res/font/spacegrotesk.ttf`

## Steps to Install

1. Visit each Google Fonts link above
2. Click "Download family" to get the .zip file
3. Extract the .zip file
4. Find the specific weight mentioned above (e.g., Lexend-Bold.ttf)
5. Copy the .ttf file to `app/src/main/res/font/`
6. Rename the file to match the names above (lowercase with underscores if needed):
   - `Lexend-Bold.ttf` → `lexend.ttf`
   - `Manrope-Regular.ttf` → `manrope.ttf`
   - `SpaceGrotesk-Medium.ttf` → `spacegrotesk.ttf`

## Verification

After placing the font files:
1. Rebuild the project: `./gradlew clean build`
2. The build should succeed without font-related errors
3. Launch the app and verify that:
   - Headlines use the elegant, condensed Lexend font
   - Body text uses the friendly Manrope font
   - Labels use the modern Space Grotesk font

## Troubleshooting

- If you see "Font file not found" errors, ensure the file names match exactly (case-sensitive on Linux/Mac)
- If fonts don't appear correctly, try invalidating Android Studio cache: File → Invalidate Caches → Invalidate and Restart
- The font family XMLs in `res/font/font_headline.xml`, `font_body.xml`, and `font_label.xml` already reference these files correctly
