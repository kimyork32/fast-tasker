'use client';

interface OfferModalProps {
  isOpen: boolean;
  budget: number;
  offerPrice: number;
  offerMessage: string;
  setOfferPrice: (price: number) => void;
  setOfferMessage: (message: string) => void;
  onClose: () => void;
  onSubmit: () => void;
}

export function OfferModal({
  isOpen,
  budget,
  offerPrice,
  offerMessage,
  setOfferPrice,
  setOfferMessage,
  onClose,
  onSubmit
}: OfferModalProps) {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      {/* Backdrop oscuro */}
      <div
        className="absolute inset-0 bg-black/60 backdrop-blur-sm transition-opacity"
        onClick={onClose}
      ></div>

      {/* Contenido del Modal */}
      <div className="relative bg-white rounded-3xl w-full max-w-md p-8 shadow-2xl transform transition-all scale-100">
        <button
          onClick={onClose}
          className="absolute top-4 right-4 p-2 text-gray-400 hover:text-gray-900 hover:bg-gray-100 rounded-full transition"
        >
          <svg
            className="w-5 h-5"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M6 18L18 6M6 6l12 12"
            />
          </svg>
        </button>

        <h2 className="text-2xl font-bold text-gray-900 mb-1">Haz tu oferta</h2>
        <p className="text-gray-500 text-sm mb-6">
          El presupuesto del cliente es{' '}
          <span className="font-bold text-gray-900">S/. {budget}</span>
        </p>

        <div className="space-y-4">
          <div>
            <label className="block text-xs font-bold text-gray-700 uppercase mb-2">
              Tu precio (S/.)
            </label>
            <input
              type="number"
              value={offerPrice}
              onChange={(e) => setOfferPrice(Number(e.target.value))}
              className="w-full text-3xl font-bold border-b-2 border-gray-200 focus:border-black outline-none py-2 px-1 text-gray-900"
            />
          </div>

          <div>
            <label className="block text-xs font-bold text-gray-700 uppercase mb-2 mt-4">
              Mensaje para el cliente
            </label>
            <textarea
              value={offerMessage}
              onChange={(e) => setOfferMessage(e.target.value)}
              className="w-full border border-gray-300 rounded-xl p-3 text-sm focus:ring-2 focus:ring-black focus:border-transparent outline-none min-h-[100px]"
              placeholder="Hola, me gustaría ayudarte con esto. Tengo experiencia en..."
            ></textarea>
          </div>

          <div className="pt-4">
            <button
              className="w-full bg-green-600 hover:bg-green-700 text-white font-bold py-4 rounded-xl shadow-lg shadow-green-900/10 transition transform active:scale-[0.98]"
              onClick={onSubmit}
            >
              Enviar Oferta
            </button>
            <p className="text-center text-xs text-gray-400 mt-3">
              Sin compromiso hasta que te acepten.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
