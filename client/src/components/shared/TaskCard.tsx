import { TaskResponse } from '@/lib/types';
import Link from 'next/link';
import { Card, CardContent, CardFooter, CardHeader } from '@/components/ui/card';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import { Badge } from '@/components/ui/badge';
import { MapPin, Calendar, Tag, Briefcase } from 'lucide-react';
import { format } from 'date-fns';

type TaskCardProps = {
  task: TaskResponse;
};

export function TaskCard({ task }: TaskCardProps) {
  const posterInitials = `${task.poster?.firstName?.[0] ?? ''}${task.poster?.lastName?.[0] ?? ''}`.toUpperCase();

  return (
    <Link href={`/tasks/${task.id}`} className="block">
      <Card className="h-full transition-all hover:shadow-xl hover:-translate-y-1 bg-slate-50 rounded-2xl border-slate-200">
        <CardHeader>
          <div className="flex justify-between items-start gap-4">
            <h3 className="font-semibold text-lg text-gray-800 flex-1">
              {task.title}
            </h3>
            <span className="text-lg font-bold text-primary whitespace-nowrap">
              S/. {task.budget.toFixed(2)}
            </span>
          </div>
        </CardHeader>
        <CardContent className="grid grid-cols-2 gap-x-4 gap-y-2 text-sm text-gray-600 p-6 pt-0">
          <div className="flex items-center gap-2">
            <MapPin className="w-4 h-4 text-gray-500" />
            <span>{task.location.address}</span>
          </div>
          <div className="flex items-center gap-2">
            <Calendar className="w-4 h-4 text-gray-500" />
            <span>{format(new Date(task.taskDate), 'dd MMM yyyy')}</span>
          </div>
          <div className="flex items-center gap-2">
            <Tag className="w-4 h-4 text-gray-500" />
            <Badge variant="outline">{task.status}</Badge>
          </div>
          {task.offerCount > 0 && (
            <div className="flex items-center gap-2">
              <Briefcase className="w-4 h-4 text-gray-500" />
              <span>{task.offerCount} {task.offerCount === 1 ? 'oferta' : 'ofertas'}</span>
            </div>
          )}
        </CardContent>
        <CardFooter className="flex justify-end">
          {task.poster && (
            <div className="flex items-center gap-2">
                <span className="text-xs text-gray-500">
                  Publicado por {task.poster.firstName}
                </span>
                <Avatar className="h-8 w-8">
                  <AvatarImage src={task.poster.photo} alt={`${task.poster.firstName} ${task.poster.lastName}`} />
                  <AvatarFallback>{posterInitials}</AvatarFallback>
                </Avatar>
            </div>
          )}
        </CardFooter>
      </Card>
    </Link>
  );
}