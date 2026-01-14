'use client';

import { OfferProfileResponse } from '@/lib/types';
import { AssignTaskerRequest } from '@/lib/types';
import { assignTasker } from '@/services/task.service';

interface OffersListProps {
  offers: OfferProfileResponse[];
  isLoading: boolean;
  isCreator: boolean;
  taskId: string;
}


const handleAssignSubmit = async (taskerId: string, taskId: string, offerId: string) => {
  const assignTaskerRequest: AssignTaskerRequest= {
    taskerId: taskerId,
    taskId: taskId,
    offerId: offerId
  };
  console.log(assignTaskerRequest);
  try {
    const newAssignResponse = await assignTasker(assignTaskerRequest);
    console.log(newAssignResponse);
    alert("Tarea asignada con éxito")
  } catch (error) {
    console.error("Error al asignar tarea:", error);
  }
}

export function OffersList({ offers, isLoading, isCreator, taskId }: OffersListProps) {
  /**
   * Formatea una fecha ISO (UTC) a una cadena de fecha y hora legible
   * en la zona horaria local del usuario.
   * @param isoString La fecha en formato ISO 8601 (ej: "2023-10-27T10:00:00Z").
   */
  const formatLocaleDate = (isoString: string) => {
    if (!isoString) return '';
    const date = new Date(isoString);
    const options: Intl.DateTimeFormatOptions = {
      day: 'numeric', month: 'short', year: 'numeric',
      hour: 'numeric', minute: '2-digit', hour12: true
    };
    return new Intl.DateTimeFormat(navigator.language, options).format(date);
  };

  if (isLoading) {
    return (
      <div className="flex justify-center items-center py-8">
        <div className="w-6 h-6 border-2 border-gray-200 border-t-black rounded-full animate-spin"></div>
      </div>
    );
  }

  if (offers.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center h-full space-y-3 py-8">
        <div className="w-16 h-16 bg-gray-50 rounded-full flex items-center justify-center border border-gray-100">
          <svg className="w-6 h-6 text-gray-300" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M20 12H4" />
          </svg>
        </div>
        <p className="text-gray-500 font-medium">No hay ofertas todavía</p>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {offers.filter(op => op && op.offer).map(offerProfile => (
        <div key={offerProfile.offer.id.toString()} className="flex gap-4 p-4 border border-gray-100 rounded-2xl">
          <div className="w-12 h-12 rounded-full bg-gray-200 flex items-center justify-center font-bold text-gray-500 shrink-0">
            {offerProfile.profile?.photo ? (
              <img src={offerProfile.profile.photo} alt={`${offerProfile.profile.firstName}`} className="w-full h-full rounded-full object-cover" />
            ) : (
              <span>
                {offerProfile.profile ? `${offerProfile.profile.firstName.charAt(0)}${offerProfile.profile.lastName.charAt(0)}` : '??'}
              </span>
            )}
          </div>
          <div className="flex-1">
            <div className="flex justify-between items-start">
              <div>
                <p className="font-bold text-gray-900">
                  {offerProfile.profile ? `${offerProfile.profile.firstName} ${offerProfile.profile.lastName}` : 'Usuario Anónimo'}
                </p>
                <p className="text-sm text-gray-500 mt-1">
                  {offerProfile.offer?.description || 'El ofertante no incluyó un mensaje.'}
                </p>
              </div>
              <div className="text-right shrink-0 pl-4">
                <p className="text-xl font-bold text-gray-900">S/. {offerProfile.offer?.price || 'N/A'}</p>
                <p className="text-xs text-gray-400">{formatLocaleDate(offerProfile.offer.createAt)}</p>
              </div>
            </div>
            {isCreator && (
              <div className="mt-4 flex justify-end">
                <button
                  className="px-4 py-2 bg-blue-600 text-white text-sm font-semibold rounded-full hover:bg-blue-700 transition-colors duration-200"
                  onClick={() => handleAssignSubmit(offerProfile.profile.id, taskId, offerProfile.offer.id)}
                >
                  Asignar Tasker
                </button>
              </div>
            )}
          </div>
        </div>
      ))}
    </div>
  );
}
