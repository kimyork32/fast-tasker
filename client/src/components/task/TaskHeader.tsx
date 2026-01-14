"use client";

interface TaskHeaderProps {
  title: string;
  status: string;
  type: string; // “Limpieza” en tu ejemplo
  publishedAgo: string; // “Publicado hace 2 días”
  views: number;
}

export function TaskHeader({
  title,
  status,
  type,
  publishedAgo,
  views,
}: TaskHeaderProps) {

  /**
   * Formatea una fecha en formato 'YYYY-MM-DD' a un texto relativo como
   * "Publicado hoy", "Publicado ayer" o "Publicado hace X días".
   * @param dateString La fecha de publicación de la tarea.
   */
  const formatRelativeDate = (dateString: string): string => {
    if (!dateString) {
      return "Fecha no disponible";
    }

    const today = new Date();
    // Se le añade T00:00:00 para que se interprete en la zona horaria local y no en UTC.
    const publicationDate = new Date(`${dateString}T00:00:00`);

    // Reseteamos la hora para comparar solo las fechas.
    today.setHours(0, 0, 0, 0);
    publicationDate.setHours(0, 0, 0, 0);

    const diffTime = today.getTime() - publicationDate.getTime();
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));

    if (diffDays === 0) {
      return "Publicado hoy";
    } else if (diffDays === 1) {
      return "Publicado ayer";
    } else if (diffDays > 1) {
      return `Publicado hace ${diffDays} días`;
    } else {
      // Para fechas en el futuro o errores de cálculo.
      return "Publicado recientemente";
    }
  };

  return (
    <div className="md:col-span-8 bg-white rounded-3xl p-6 md:p-8 shadow-sm border border-gray-200/60 flex flex-col justify-between min-h-[200px]">
      <div>
        {/* Labels de estado y tipo */}
        <div className="flex gap-3 mb-4">
          <span
            className={`px-3 py-1 rounded-full text-xs font-bold border ${
              status === "OPEN"
                ? "bg-green-50 text-green-700 border-green-200"
                : "bg-gray-50 text-gray-600 border-gray-200"
            }`}
          >
            {status}
          </span>

          <span className="px-3 py-1 rounded-full text-xs font-bold bg-blue-50 text-blue-700 border border-blue-200">
            {type}
          </span>
        </div>

        {/* Título */}
        <h1 className="text-3xl md:text-5xl font-extrabold text-gray-900 tracking-tight leading-tight">
          {title}
        </h1>
      </div>

      {/* Publicado + vistas */}
      <div className="mt-6 flex items-center gap-2 text-gray-400 text-sm">
        <span>{formatRelativeDate(publishedAgo)}</span>
        <span>•</span>
        <span>{views} visualizaciones</span>
      </div>
    </div>
  );
}
