'use client'

interface TaskDescriptionProps {
    description: string;
    images?: string[];
}

export function TaskDescription({
    description,
    images = []
}: TaskDescriptionProps) {
    return (
        <div className="md:col-span-8 bg-white rounded-3xl p-6 md:p-8 shadow-sm border border-gray-200/60">
            <h3 className="text-xl font-bold text-gray-900 mb-4">Detalles de la tarea</h3>
            <div className="prose prose-slate max-w-none text-gray-600 leading-relaxed">
                <p className="whitespace-pre-wrap">{description}</p>
            </div>
            {images.length > 0 && (
                <div className="mt-8">
                    <p className="text-sm font-bold text-gray-900 mb-3">Imágenes de referencia</p>
                    <div className="flex gap-2 overflow-x-auto pb-2">
                        {images.map((img, index) => (
                            <img
                                key={index}
                                src={img}
                                alt={`Imagen ${index + 1}`}
                                className="rounded-md w-full h-auto object-cover"
                            />
                        ))}
                    </div>
                </div>
            )}
        </div>
    );
}
