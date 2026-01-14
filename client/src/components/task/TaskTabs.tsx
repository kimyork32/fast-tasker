'use client';

import { OffersList } from './OffersList';
import { OfferProfileResponse, QuestionProfileResponse } from '@/lib/types';
import { QuestionsList } from './QuestionsList';

interface Question {
  id: string;
  author: string;
  avatarColor?: string;
  message: string;
  timestamp: string;
  answer?: { author: string; message: string; timestamp: string };
}

interface TaskTabsProps {
  offers: OfferProfileResponse[];
  questions: QuestionProfileResponse[];
  isLoadingOffers: boolean;
  numOffers: number;
  numQuestions: number;
  activeTab: 'offers' | 'questions';
  questionDescription: string;
  onTabChange: (tab: 'offers' | 'questions') => void;
  onQuestionSubmit: () => void;
  isCreator: boolean;
  taskId: string;
  onAnswerSubmit: (questionId: string, answerDescription: string) => void;
  setQuestionDescription: (description: string) => void;
}

export function TaskTabs({
  offers,
  isLoadingOffers,
  questions,
  numOffers,
  numQuestions,
  activeTab,
  questionDescription,
  onTabChange,
  isCreator,
  onQuestionSubmit,
  setQuestionDescription,  
  taskId,
  onAnswerSubmit
}: TaskTabsProps) {
  return (
    <div className="md:col-span-12 bg-white rounded-3xl p-2 shadow-sm border border-gray-200/60">

      {/* BOTONES */}
      <div className="flex bg-gray-100 p-1 rounded-2xl w-full md:w-fit mb-6">
        
        {/* boton OFERTAS */}
        <button
          onClick={() => onTabChange('offers')}
          className={`flex-1 md:flex-none px-6 py-3 rounded-xl text-sm font-bold transition-all ${
            activeTab === 'offers' ? 'bg-white text-black shadow-sm' : 'text-gray-500 hover:text-gray-700'
          }`}
        >
          Ofertas ({offers.length > 0 ? offers.length : numOffers})
        </button>

        {/* boton PREGUNTAS */}
        <button
          onClick={() => onTabChange('questions')}
          className={`flex-1 md:flex-none px-6 py-3 rounded-xl text-sm font-bold transition-all ${
            activeTab === 'questions' ? 'bg-white text-black shadow-sm' : 'text-gray-500 hover:text-gray-700'
          }`}
        >
          Preguntas ({numQuestions})
        </button>
      </div>

      {/* LISTA DE OFERTAS Y PREGUNTAS */}
      <div className="px-6 pb-8 min-h-[200px]">
        {activeTab === 'offers' ? (
          <OffersList offers={offers} isLoading={isLoadingOffers} isCreator={isCreator} taskId={taskId} />
        ) : (
          <QuestionsList 
            questions={questions} 
            questionDescription={questionDescription}
            onQuestionSubmit={onQuestionSubmit} 
            setQuestionDescription={setQuestionDescription}
            onAnswerSubmit={onAnswerSubmit}
            isCreator={isCreator}
          />
        )}
      </div>
    </div>
  );
}
