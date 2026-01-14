// src/app/(dashboard)/tasks/page.tsx
"use client"; // <-- Convertido a Componente de Cliente

import { useState, useEffect } from 'react';
import { TaskResponse } from '@/lib/types'; // Importa tu tipo
import { getMyTasks } from '@/services/task.service'; // ¡Importamos el servicio REAL!
import { TaskCard } from '@/components/shared/TaskCard'; // Asumiendo que tienes este componente

export default function MyTasksPage() {
  const [tasks, setTasks] = useState<TaskResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const loadTasks = async () => {
      try {
        setIsLoading(true);
        const data = await getMyTasks(); 
        setTasks(data);
      } catch (err) {
        setError((err as Error).message);
      } finally {
        setIsLoading(false);
      }
    };

    loadTasks();
  }, []);

  if (isLoading) {
    return <div>Cargando tareas...</div>;
  }

  if (error) {
    return <div style={{ color: 'red' }}>Error: {error}</div>;
  }

  return (
    <div className="flex flex-col gap-6">
      <h1 className="text-3xl font-bold">Mis Tareas Creadas</h1>

      {tasks.length > 0 ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {tasks.map((task) => (
            <TaskCard key={task.id} task={task} />
          ))}
        </div>
      ) : (
        <p>Aún no has creado ninguna tarea.</p>
      )}
    </div>
  );
}