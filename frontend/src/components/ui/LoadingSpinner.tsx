export default function LoadingSpinner({ size = "md" }: { size?: "sm" | "md" | "lg" }) {
  const sizeClass = { sm: "w-5 h-5", md: "w-8 h-8", lg: "w-12 h-12" }[size];
  return (
    <div className="flex justify-center items-center p-8">
      <div
        className={`${sizeClass} border-4 border-indigo-200 dark:border-indigo-800 border-t-indigo-600 rounded-full animate-spin`}
      />
    </div>
  );
}
