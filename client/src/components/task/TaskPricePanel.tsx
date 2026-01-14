'use client';

interface TaskPricePanelProps {
    budget: number;
    onOpenOfferModal: () => void;
}

export function TaskPricePanel({
    budget,
    onOpenOfferModal
} : TaskPricePanelProps) {
    return (
        <div className="md:col-span-4 bg-gray-900 rounded-3xl p-6 md:p-8 shadow-xl text-white flex flex-col justify-between relative overflow-hidden group">
          <div className="absolute top-0 right-0 w-32 h-32 bg-blue-500 rounded-full blur-[60px] opacity-20 group-hover:opacity-40 transition duration-500"></div>

          <div>
            <p className="text-gray-400 text-sm font-medium mb-1">Presupuesto</p>
            <div className="text-4xl md:text-5xl font-bold tracking-tighter">
              <span className="text-2xl text-gray-500 align-top mr-1">S/.</span>
              {budget}
            </div>
          </div>

          <button 
            className="mt-8 w-full bg-white text-black font-bold py-4 rounded-xl hover:bg-gray-200 transition-colors flex items-center justify-center gap-2"
            onClick={onOpenOfferModal} // <--- AQUI ABRIMOS EL MODAL
          >
            Hacer Oferta
            <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M14 5l7 7m0 0l-7 7m7-7H3" /></svg>
          </button>
        </div>
    )
}
