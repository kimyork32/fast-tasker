// src/app/(dashboard)/tasks/page.tsx
"use client"; // <-- Convertido a Componente de Cliente

import { useState, useEffect, useMemo } from 'react';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Search, Filter, ChevronDown } from 'lucide-react';
import { TaskResponse } from '@/lib/types'; // Importa tu tipo
import { getPublicTasks } from '@/services/task.service';
import { TaskCard } from '@/components/shared/TaskCard'; // Asumiendo que tienes este componente

export default function PublicTasksPage() {
  const [tasks, setTasks] = useState<TaskResponse[]>([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [sortBy, setSortBy] = useState('newest');
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const loadTasks = async () => {
      try {
        setIsLoading(true);
        const data = await getPublicTasks();
        setTasks(data);
      } catch (err) {
        setError((err as Error).message);
      } finally {
        setIsLoading(false);
      }
    };

    loadTasks();
  }, []); // El array vacío [] asegura que se ejecute solo 1 vez

  const filteredAndSortedTasks = useMemo(() => {
    return tasks
      .filter((t) =>
        !searchTerm
          ? true
          : t.title.toLowerCase().includes(searchTerm.toLowerCase())
      )
      .sort((a, b) => {
        switch (sortBy) {
          case 'budget-desc':
            return b.budget - a.budget;
          case 'budget-asc':
            return a.budget - b.budget;
          case 'newest':
          default:
            return new Date(b.taskDate).getTime() - new Date(a.taskDate).getTime();
        }
      });
  }, [tasks, searchTerm, sortBy]);


  if (isLoading) {
    return <div>Cargando tareas...</div>;
  }

  if (error) {
    return <div style={{ color: 'red' }}>Error: {error}</div>;
  }

  return (
    <div className="flex flex-col gap-6">
      <div className="flex flex-col items-start gap-4 md:flex-row md:items-center md:justify-between">
        <h1 className="text-3xl font-bold shrink-0">Encuentra Tareas</h1>
        <div className="flex items-center gap-2 w-full md:w-auto">
          <div className="relative flex-grow">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-5 w-5 text-muted-foreground" />
            <Input
              type="search"
              placeholder="Buscar..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="h-11 w-full pl-10"
            />
          </div>
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant="outline" className="h-11 shrink-0">
                Ordenar por
                <ChevronDown className="w-4 h-4 ml-2" />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent>
              <DropdownMenuLabel>Ordenar por</DropdownMenuLabel>
              <DropdownMenuSeparator />
              <DropdownMenuItem onSelect={() => setSortBy('newest')}>Más reciente</DropdownMenuItem>
              <DropdownMenuItem onSelect={() => setSortBy('budget-desc')}>Presupuesto: Mayor a menor</DropdownMenuItem>
              <DropdownMenuItem onSelect={() => setSortBy('budget-asc')}>Presupuesto: Menor a mayor</DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
          <Button variant="outline" className="h-11 shrink-0">
            <Filter className="w-4 h-4 mr-2" />
            Filtro
          </Button>
        </div>
      </div>

      {filteredAndSortedTasks.length > 0 ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {filteredAndSortedTasks.map((task) => (<TaskCard key={task.id} task={task} />))}
        </div>
      ) : (
        <p>No hay tareas disponibles en este momento.</p>
      )}

    </div>
  );
}