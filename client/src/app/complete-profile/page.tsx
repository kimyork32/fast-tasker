"use client"

import { FormEvent, useState } from 'react'
import { useRouter } from 'next/navigation'
import Cookies from 'js-cookie'
import { completeProfile } from '@/services/account.service' // Importa el servicio
import { CompleteProfileRequest } from '@/lib/types' // Importa el tipo
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card'
import AutocompleteDistrito from "@/components/shared/AutocompleteDistrito"
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Button } from '@/components/ui/button'

export default function CompleteProfilePage() {
  const router = useRouter()
  const [firstName, setFirstName] = useState('')
  const [lastName, setLastName] = useState('')
  const [zip, setZip] = useState('') // Manejar siempre como string
  const [distrito, setDistrito] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [locationType, setLocationType] = useState<'distrito' | 'zip'>('distrito')

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError(null)
    setIsSubmitting(true)

    console.log(`zip code: ${zip}`);
    const profileData: CompleteProfileRequest = {
      profile: {
        firstName,
        lastName,
        photo: '',
        about: '',
        location: {
          latitude: 0,
          longitude: 0,
          address: distrito, // Usamos el estado 'distrito' para la dirección
          zip: Number(zip) // Convertimos a número justo antes de enviar
        }
      }
    }

    try {
      const response = await completeProfile(profileData)
      Cookies.set('jwtToken', response.token, { expires: 1, path: '/' })
      router.replace('/dashboard')
    } catch (err) {
      console.error('Error completando perfil:', err)
      setError((err as Error).message || 'Ocurrió un error inesperado.')
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div className="flex h-full min-h-0 items-start justify-center bg-muted/40 pt-50">
      <Card className="mx-auto w-full max-w-lg p-6">
        <CardHeader>
          <CardTitle className="text-2xl">Completa tu perfil</CardTitle>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="firstName">Nombres</Label>
                <Input id="firstName" value={firstName} onChange={e => setFirstName(e.target.value)} placeholder='Juan' required />
              </div>
              <div className="space-y-2">
                <Label htmlFor="lastName">Apellidos</Label>
                <Input id="lastName" value={lastName} onChange={e => setLastName(e.target.value)} placeholder='Flores Minaya' required />
              </div>
            </div>

            <div className="space-y-2">
              <div className="flex items-center justify-between">
                <Label htmlFor="location">{locationType === 'distrito' ? 'Distrito' : 'Código ZIP'}</Label>
                <button
                  type="button"
                  onClick={() => setLocationType(locationType === 'distrito' ? 'zip' : 'distrito')}
                  className="text-sm text-muted-foreground"
                >
                  {locationType === 'distrito' ? 'Insertar código ZIP' : 'Insertar distrito'}
                </button>
              </div>
              {locationType === 'distrito' ? (
                <AutocompleteDistrito
                  value={distrito}
                  onSelect={(item) => {
                    // Construimos la cadena completa para la dirección
                    const fullAddress = `${item.distrito}, ${item.provincia}, ${item.departamento}`;
                    setDistrito(fullAddress); // Guardamos la dirección completa
                    setZip(String(item.zip_code)); // Guardamos el ZIP
                  }}
                />
              ) : (
                <Input id="zip" type="number" value={zip} onChange={e => setZip(e.target.value)} placeholder='15063' required />
              )}
            </div>

            {error && <div className="text-sm text-red-500">{error}</div>}

            <div className="pt-2">
              <Button type="submit" className="w-full" disabled={isSubmitting}>{isSubmitting ? 'Enviando...' : 'Completar perfil'}</Button>
            </div>
          </form>
        </CardContent>
      </Card>
    </div>
  )
}
