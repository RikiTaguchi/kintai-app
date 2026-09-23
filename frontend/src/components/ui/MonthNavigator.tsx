interface Props {
  year: number;
  month: number;
  onPrev: () => void;
  onNext: () => void;
  suffix?: string;
}

export default function MonthNavigator({ year, month, onPrev, onNext, suffix = "" }: Props) {
  return (
    <div className="flex items-center justify-between mb-4">
      <button onClick={onPrev} className="p-2 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700 text-gray-600 dark:text-gray-400 cursor-pointer">
        ‹
      </button>
      <h1 className="text-lg font-bold text-gray-900 dark:text-gray-100">
        {year}年{month}月{suffix}
      </h1>
      <button onClick={onNext} className="p-2 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700 text-gray-600 dark:text-gray-400 cursor-pointer">
        ›
      </button>
    </div>
  );
}
