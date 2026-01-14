'use client';

import { useState } from 'react';
import { 
  QuestionProfileResponse,
  AnswerProfileResponse,
} from "@/lib/types";

interface QuestionsListProps {
  questions: QuestionProfileResponse[];
  // Props para la caja de "hacer una pregunta"
  questionDescription: string;
  setQuestionDescription: (description: string) => void;
  onQuestionSubmit: () => void;
  // Props para la caja de "responder una pregunta"
  onAnswerSubmit: (questionId: string, answerDescription: string) => void;
  isCreator: boolean;
}

export function QuestionsList({ 
  questions,
  questionDescription,
  setQuestionDescription,
  onQuestionSubmit,
  onAnswerSubmit,
  isCreator
}: QuestionsListProps) {
  const [activeReplyId, setActiveReplyId] = useState<string | null>(null);
  const [replyDescription, setReplyDescription] = useState('');

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

  return (
    <div className="max-w-3xl">
      {/* Caja para escribir pregunta */}
      <div className="flex gap-4 mb-10">
        <div className="w-10 h-10 rounded-full bg-blue-100 flex items-center justify-center text-blue-600 font-bold shrink-0">YO</div>
        <div className="flex-1">
          <textarea
            value={questionDescription}
            onChange={(e) => setQuestionDescription(e.target.value)}
            rows={3}
            placeholder="Haz una pregunta pública sobre esta tarea..."
            className="w-full border border-gray-200 rounded-xl p-4 text-sm focus:ring-2 focus:ring-black focus:border-transparent outline-none resize-none bg-gray-50"
          ></textarea>
          <div className="flex justify-end mt-2">
            <button 
              className="px-6 py-2 bg-blue-600 text-white text-sm font-bold rounded-full hover:bg-blue-700 transition"
              onClick={onQuestionSubmit}
            >
              Publicar pregunta
            </button>
          </div>
        </div>
      </div>

      {/* Lista de preguntas */}
      <div className="space-y-8">
        {
          questions.length === 0 ? (
            <div className="flex flex-col items-center justify-center h-full space-y-3 py-8">
              <div className="w-16 h-16 bg-gray-50 rounded-full flex items-center justify-center border border-gray-100">
                <svg className="w-6 h-6 text-gray-300" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M20 12H4" />
                </svg>
              </div>
              <p className="text-gray-500 font-medium">No hay preguntas todavía</p>
            </div>
          ) : (
            questions.map(q => (
              <div key={q.question.id} className="flex gap-4 group">
                <div className="w-10 h-10 rounded-full bg-gray-200 flex items-center justify-center text-xs font-bold shrink-0 overflow-hidden">
                  {q.profile.photo ? (
                    <img src={q.profile.photo} alt={q.profile.firstName} className="w-full h-full object-cover" />
                  ) : (
                    <span className="text-gray-500">{`${q.profile.firstName.slice(0, 2)}${q.profile.lastName.slice(0, 2)}`.toUpperCase()}</span>
                  )}
                </div>
                <div className="flex-1">
                  <div className="flex items-center gap-2 mb-1">
                    <span className="font-bold text-gray-900 text-sm">{`${q.profile.firstName} ${q.profile.lastName}`}</span>
                    <span className="text-xs text-gray-400">{formatLocaleDate(q.question.createAt)}</span>
                  </div>
                  <p className="text-gray-700 text-sm leading-relaxed">{q.question.description}</p>

                  {/* Botón de Responder y formulario de respuesta */}
                  <div className="mt-3">
                    <button 
                      onClick={() => {
                        setActiveReplyId(activeReplyId === q.question.id ? null : q.question.id);
                        setReplyDescription(''); // Limpiar al abrir/cerrar
                      }}
                      className="text-xs font-bold text-blue-600 hover:underline"
                    >
                      Responder
                    </button>

                    {activeReplyId === q.question.id && (
                      <div className="flex gap-4 mt-4">
                        <div className="w-8 h-8 rounded-full bg-blue-100 flex items-center justify-center text-blue-600 font-bold shrink-0 text-xs">YO</div>
                        <div className="flex-1">
                          <textarea
                            value={replyDescription}
                            onChange={(e) => setReplyDescription(e.target.value)}
                            rows={2}
                            placeholder="Escribe tu respuesta..."
                            className="w-full border border-gray-200 rounded-xl p-3 text-sm focus:ring-2 focus:ring-black focus:border-transparent outline-none resize-none bg-gray-50"
                          ></textarea>
                          <div className="flex justify-end mt-2">
                            <button className="px-5 py-1.5 bg-black text-white text-xs font-bold rounded-full hover:bg-gray-800 transition" onClick={() => {
                              onAnswerSubmit(q.question.id, replyDescription);
                              setReplyDescription('');
                              setActiveReplyId(null);
                            }}>
                              Publicar respuesta
                            </button>
                          </div>
                        </div>
                      </div>
                    )}
                  </div>

                  {q.answers != null && q.answers.length > 0 && (
                    <div className="mt-4 space-y-4">
                      {q.answers.map((ans) => (
                         <div 
                            key={ans.answer.id}
                            className="flex gap-3 pl-4 border-l-2 border-gray-100"
                          >
                        <div className="w-6 h-6 rounded-full bg-black text-white flex items-center justify-center text-[10px] font-bold shrink-0">
                          {`${ans.profile.firstName.slice(0, 1)}${ans.profile.lastName.slice(0, 1)}`.toUpperCase()}
                        </div>
                        <div>
                          <div className="flex items-center gap-2 mb-1">
                            <span className="font-bold text-black text-xs">
                              {`${ans.profile.firstName} ${ans.profile.lastName}`}
                            </span>
                            <span className="text-xs text-gray-400">
                              {formatLocaleDate(ans.answer.createdAt)}
                            </span>
                          </div>

                          <p className="text-gray-600 text-sm">
                            {ans.answer.description}
                          </p>
                        </div>
                      </div>
                      ))}
                    </div>
                  )}
                </div>
              </div>
            ))
          )
        }  
      </div>
    </div>
  );
}
