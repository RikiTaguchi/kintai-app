import Link from "next/link";

export default function Home() {
  return (
    <div className="min-h-screen flex flex-col items-center justify-center bg-gray-50 dark:bg-gray-900 p-6">
      <div className="text-center mb-10">
        <h1 className="text-3xl font-bold text-gray-900 dark:text-gray-100 mb-2">勤怠管理システム</h1>
        <p className="text-gray-500 dark:text-gray-400">明光義塾</p>
      </div>
      <div className="flex flex-col sm:flex-row gap-4 w-full max-w-sm">
        <Link
          href="/manager/login"
          className="flex-1 flex flex-col items-center gap-2 bg-indigo-600 text-white py-5 px-6 rounded-xl hover:bg-indigo-700 transition-colors text-center"
        >
          <svg className="w-8 h-8" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" />
          </svg>
          <span className="font-semibold">管理者ログイン</span>
          <span className="text-indigo-200 text-xs">教室長・スタッフ向け</span>
        </Link>
        <Link
          href="/tutor/login"
          className="flex-1 flex flex-col items-center gap-2 bg-teal-600 text-white py-5 px-6 rounded-xl hover:bg-teal-700 transition-colors text-center"
        >
          <svg className="w-8 h-8" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
          </svg>
          <span className="font-semibold">講師ログイン</span>
          <span className="text-teal-200 text-xs">講師・チューター向け</span>
        </Link>
      </div>
    </div>
  );
}
