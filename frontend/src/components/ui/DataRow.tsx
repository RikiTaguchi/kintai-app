interface Props {
  label: string;
  value: string;
  valueClass?: string;
}

export default function DataRow({ label, value, valueClass = "text-gray-900 dark:text-gray-100" }: Props) {
  return (
    <div className="flex items-center px-5 py-4">
      <span className="text-sm text-gray-500 dark:text-gray-400 w-28 shrink-0">{label}</span>
      <span className={`text-sm font-medium ${valueClass}`}>{value}</span>
    </div>
  );
}
