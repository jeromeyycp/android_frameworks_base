/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include <SkTypeface.h>
#include <SkPaint.h>

#define LOG_TAG "Minikin"
#include <cutils/log.h>

#include <minikin/MinikinFont.h>
#include "MinikinSkia.h"

namespace android {

MinikinFontSkia::MinikinFontSkia(SkTypeface *typeface) :
    mTypeface(typeface) {
}

MinikinFontSkia::~MinikinFontSkia() {
    SkSafeUnref(mTypeface);
}

bool MinikinFontSkia::GetGlyph(uint32_t codepoint, uint32_t *glyph) const {
    SkPaint paint;
    paint.setTypeface(mTypeface);
    paint.setTextEncoding(SkPaint::kUTF32_TextEncoding);
    uint16_t glyph16;
    paint.textToGlyphs(&codepoint, sizeof(codepoint), &glyph16);
    *glyph  = glyph16;
    return !!glyph;
}

static void MinikinFontSkia_SetSkiaPaint(SkTypeface* typeface, SkPaint* skPaint, const MinikinPaint& paint) {
    skPaint->setTypeface(typeface);
    skPaint->setTextEncoding(SkPaint::kGlyphID_TextEncoding);
    // TODO: set more paint parameters from Minikin
    skPaint->setTextSize(paint.size);
}

float MinikinFontSkia::GetHorizontalAdvance(uint32_t glyph_id,
    const MinikinPaint &paint) const {
    SkPaint skPaint;
    uint16_t glyph16 = glyph_id;
    SkScalar skWidth;
    MinikinFontSkia_SetSkiaPaint(mTypeface, &skPaint, paint);
    skPaint.getTextWidths(&glyph16, sizeof(glyph16), &skWidth, NULL);
#ifdef VERBOSE
    ALOGD("width for typeface %d glyph %d = %f", mTypeface->uniqueID(), glyph_id, skWidth);
#endif
    return skWidth;
}

void MinikinFontSkia::GetBounds(MinikinRect* bounds, uint32_t glyph_id,
    const MinikinPaint& paint) const {
    SkPaint skPaint;
    uint16_t glyph16 = glyph_id;
    SkRect skBounds;
    MinikinFontSkia_SetSkiaPaint(mTypeface, &skPaint, paint);
    skPaint.getTextWidths(&glyph16, sizeof(glyph16), NULL, &skBounds);
    bounds->mLeft = skBounds.fLeft;
    bounds->mTop = skBounds.fTop;
    bounds->mRight = skBounds.fRight;
    bounds->mBottom = skBounds.fBottom;
}

bool MinikinFontSkia::GetTable(uint32_t tag, uint8_t *buf, size_t *size) {
    if (buf == NULL) {
        const size_t tableSize = mTypeface->getTableSize(tag);
        *size = tableSize;
        return tableSize != 0;
    } else {
        const size_t actualSize = mTypeface->getTableData(tag, 0, *size, buf);
        *size = actualSize;
        return actualSize != 0;
    }
}

SkTypeface *MinikinFontSkia::GetSkTypeface() {
    return mTypeface;
}

int32_t MinikinFontSkia::GetUniqueId() const {
    return mTypeface->uniqueID();
}

}