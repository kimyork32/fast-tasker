"use client"

import { useState, useRef, useEffect } from "react"
import { cn } from "@/lib/utils"
import Fuse from "fuse.js"
import { Input } from "@/components/ui/input"
import rawData  from "@/data/zip_codes.json"

interface Props {
  value: string
  onSelect: (item: any) => void
}

// Función para “desdoblar” distritos
function preprocessDistritos(data: typeof rawData) {
  const result: any[] = []

  data.forEach(item => {
    const distritosArray = item.distritos.split(",").map(d => d.trim())
    distritosArray.forEach(distrito => {
      result.push({
        zip_code: item.zip_code,
        distrito,
        provincia: item.provincia,
        departamento: item.departamento
      })
    })
  })

  return result
}

export default function AutocompleteDistrito({ value, onSelect }: Props) {
  const [results, setResults] = useState<any[]>([])
  const [query, setQuery] = useState(value)
  const [highlightedIndex, setHighlightedIndex] = useState(-1)
  const listRef = useRef<HTMLUListElement>(null)

  const data = preprocessDistritos(rawData)

  const fuse = new Fuse(data, {
    keys: ["distrito", "provincia", "departamento"],
    threshold: 0.3,
  })

  // Reiniciar el índice resaltado cuando los resultados cambian
  useEffect(() => {
    setHighlightedIndex(-1)
  }, [results])

  function handleChange(text: string) {
    setQuery(text)

    if (!text.trim()) {
      setResults([])
      return
    }

    const matches = fuse.search(text).slice(0, 7)
    setResults(matches.map(m => m.item))
  }

  function handleSelect(item: any) {
    setQuery(`${item.distrito}, ${item.provincia}, ${item.departamento}, ${item.zip_code}`)
    setResults([])
    setHighlightedIndex(-1)
    onSelect(item) // devolvemos el distrito+provincia+departamento+zip al padre
  }

  function handleKeyDown(e: React.KeyboardEvent<HTMLInputElement>) {
    if (results.length === 0) return

    switch (e.key) {
      case "ArrowDown":
        e.preventDefault()
        setHighlightedIndex(prev => (prev < results.length - 1 ? prev + 1 : 0))
        break
      case "ArrowUp":
        e.preventDefault()
        setHighlightedIndex(prev => (prev > 0 ? prev - 1 : results.length - 1))
        break
      case "Enter":
        if (highlightedIndex > -1) {
          e.preventDefault()
          handleSelect(results[highlightedIndex])
        }
        break
      case "Escape":
        setResults([])
        break
    }

    // Scroll para mantener el elemento visible
    const highlightedElement = listRef.current?.children[highlightedIndex]
    highlightedElement?.scrollIntoView({ block: "nearest" })
  }

  return (
    <div className="relative w-full">
      <Input
        value={query}
        onKeyDown={handleKeyDown}
        onChange={e => handleChange(e.target.value)}
        placeholder="Miraflores"
        autoComplete="off"
      />

      {results.length > 0 && (
        <ul
          ref={listRef}
          className="absolute z-20 mt-1 max-h-60 w-full overflow-y-auto rounded border bg-background shadow-lg"
        >
          {results.map((item, idx) => (
            <li
              key={idx}
              onClick={() => handleSelect(item)}
              onMouseEnter={() => setHighlightedIndex(idx)}
              className={cn(
                "cursor-pointer px-3 py-2",
                idx === highlightedIndex ? "bg-accent text-accent-foreground" : "hover:bg-muted"
              )}
            >
              <strong>{item.distrito}</strong> — {item.provincia}, {item.departamento}, {item.zip_code}
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}
