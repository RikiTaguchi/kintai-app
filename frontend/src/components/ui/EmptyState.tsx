interface Props {
  message: string;
  subMessage?: string;
}

export default function EmptyState({ message, subMessage }: Props) {
  return (
    <div className="text-center py-12 text-gray-400 dark:text-gray-500">
      <p className="text-sm">{message}</p>
      {subMessage && <p className="text-xs mt-1">{subMessage}</p>}
    </div>
  );
}
