
"use client";
import React, { useState } from 'react';
import Title from './title/Title';
import DateStep from './date/Date';
import Location from './location/Location';
import Details from './details/Details';
import Budget from './budget/Budget';
import { createTask } from '@/services/task.service';
import { useRouter } from 'next/navigation';

export default function NewTaskPage() {
  const [step, setStep] = useState(0);
  const [title, setTitle] = useState('');
  const [date, setDate] = useState('');
  const [location, setLocation] = useState('');
  const [zip, setZip] = useState(''); // Nuevo estado para el código postal
  const [details, setDetails] = useState('');
  const [budget, setBudget] = useState('');
  const [error, setError] = useState<string | null>(null);
  const router = useRouter();

  // Handler para crear la tarea
  const handleCreateTask = async () => {
    setError(null);
    try {
      // Construir el objeto TaskRequest
      const taskData = {
        title,
        description: details,
        budget: Number(budget),
        location: {
          latitude: 0,
          longitude: 0,
          address: location,
          zip: Number(zip)
        },
        taskDate: date,
      };
      await createTask(taskData);
      router.push('/dashboard');
    } catch (err) {
      setError((err as Error).message);
    }
  };

  const steps = [
    <Title
      key="title"
      title={title}
      setTitle={setTitle}
      onNext={() => setStep(1)}
    />,
    <DateStep
      key="date"
      date={date}
      setDate={setDate}
      onBack={() => setStep(0)}
      onNext={() => setStep(2)}
    />,
    <Location
      key="location"
      location={location}
      onBack={() => setStep(1)}
      onNext={(data) => {
        setLocation(data.location);
        setZip(data.zip);
        setStep(3);
      }}
    />,
    <Details
      key="details"
      details={details}
      setDetails={setDetails}
      onBack={() => setStep(2)}
      onNext={() => setStep(4)}
    />,
    <Budget
      key="budget"
      budget={budget}
      setBudget={setBudget}
      onBack={() => setStep(3)}
      onSubmit={handleCreateTask}
    />,
  ];

  const stepLabels = ['Título', 'Fecha', 'Ubicación', 'Detalles', 'Precio'];

  return (
    <div className="flex flex-col items-center w-full p-4 md:p-6 bg-gray-50 min-h-[70vh]">
      <div className="w-full max-w-2xl">
        <h2 className="text-xl font-bold mb-4">Publica una tarea</h2>
        <div className="mb-6">
          <div className="flex items-center">
            {stepLabels.map((label, index) => (
              <React.Fragment key={label}>
                <div
                  className={`flex flex-col items-center ${index < step ? 'cursor-pointer hover:opacity-80' : 'cursor-default'}`}
                  onClick={() => {
                    if (index < step) setStep(index);
                  }}
                >
                  <div
                    className={`flex items-center justify-center w-6 h-6 rounded-full text-base font-semibold ${
                      step >= index
                        ? 'bg-black text-white'
                        : 'bg-gray-300 text-gray-600'
                    }`}
                  >
                    {index + 1}
                  </div>
                  <p
                    className={`mt-1 text-xs text-center ${
                      step >= index ? 'font-bold text-black' : 'text-gray-500'
                    }`}
                  >
                    {label}
                  </p>
                </div>
                {index < stepLabels.length - 1 && (
                  <div
                    className={`flex-auto border-t-2 transition-colors duration-500 ease-in-out ${
                      step > index ? 'border-black' : 'border-gray-300'
                    }`}
                  ></div>
                )}
              </React.Fragment>
            ))}
          </div>
        </div>
        <main className="bg-white p-6 rounded-lg shadow-md">
          {steps[step]}
          {error && <div className="text-red-500 mt-4">{error}</div>}
        </main>
      </div>
    </div>
  );
}
