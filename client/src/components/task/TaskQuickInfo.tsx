'use client'

interface TaskQuickInfoProps {
    date: string;
    location: string;
}

export function TaskQuickInfo({
    date,
    location
} : TaskQuickInfoProps) {
    return (
        <div className="md:col-span-4 bg-white rounded-3xl p-6 shadow-sm border border-gray-200/60 space-y-6">
            <div className="flex items-start gap-4">
                <div className="w-10 h-10 rounded-full bg-orange-50 flex items-center justify-center text-orange-500 shrink-0">
                    <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" /></svg>
                </div>
                <div>
                    <p className="text-xs font-bold text-gray-400 uppercase">Cuándo</p>
                    <p className="font-semibold text-gray-900">{date}</p> {/* Accede a taskDate desde taskComplete.task */}
                </div>
            </div>
            {location && ( // Accede a la ubicación desde taskComplete.task
                <div className="flex items-start gap-4">
                    <div className="w-10 h-10 rounded-full bg-purple-50 flex items-center justify-center text-purple-500 shrink-0">
                        <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" /><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" /></svg>
                    </div>
                    <div>
                        <p className="text-xs font-bold text-gray-400 uppercase">Dónde</p>
                        <p className="font-semibold text-gray-900 leading-snug">{location}</p> {/* Accede a la dirección desde taskComplete.task.location */}
                    </div>
                </div>
            )}
        </div>
    )
}