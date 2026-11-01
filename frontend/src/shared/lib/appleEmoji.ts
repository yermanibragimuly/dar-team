const APPLE_EMOJI_CDN = 'https://cdn.jsdelivr.net/npm/emoji-datasource-apple@15.1.2/img/apple/64/'

function getEmojiHex(emojiStr: string): string {
  const codePoints: string[] = []
  for (let i = 0; i < emojiStr.length; i++) {
    const code = emojiStr.codePointAt(i)
    if (code !== undefined) {
      codePoints.push(code.toString(16))
      if (code > 0xffff) {
        i++ // Пропускаем второй символ суррогатной пары
      }
    }
  }

  // Для составных эмодзи с оттенками кожи (1f3fb..1f3ff) в CDN Apple селектор fe0f опускается
  const hasSkinTone = codePoints.some(cp => ['1f3fb', '1f3fc', '1f3fd', '1f3fe', '1f3ff'].includes(cp))
  const filtered = hasSkinTone ? codePoints.filter(cp => cp !== 'fe0f') : codePoints

  return filtered.join('-')
}

// Регулярное выражение охватывает:
// 1. Флаги стран (пары региональных букв 🇦🇿, 🇰🇿, 🇺🇸)
// 2. Эмодзи с модификаторами цвета кожи (👊🏻, 💪🏼, 👍🏽)
// 3. Составные последовательности ZWJ и вариации
const EMOJI_REGEX = /(?:\uD83C[\uDDE6-\uDDFF]){2}|(?:\p{Regional_Indicator}){2}|(?:[\p{Extended_Pictographic}\u2600-\u27BF][\p{Emoji_Modifier}\uFE0F]*(?:\u200D[\p{Extended_Pictographic}\p{Emoji_Modifier}\uFE0F]+)*)/gu

export function renderAppleEmoji(text: string | null | undefined): string {
  if (!text) return ''

  return text.replace(EMOJI_REGEX, (match) => {
    let hex = getEmojiHex(match)
    if (!hex) return match

    const url = `${APPLE_EMOJI_CDN}${hex}.png`
    const altHex = hex.includes('-fe0f') ? hex.replace(/-fe0f/g, '') : `${hex}-fe0f`
    const altUrl = `${APPLE_EMOJI_CDN}${altHex}.png`

    return `<img src="${url}" alt="${match}" class="inline-block w-[1.25em] h-[1.25em] align-[-0.2em] mx-[1.5px] pointer-events-none select-none" loading="lazy" onerror="if(this.src!=='${altUrl}'){this.src='${altUrl}';}else{this.outerHTML='${match}';}" />`
  })
}