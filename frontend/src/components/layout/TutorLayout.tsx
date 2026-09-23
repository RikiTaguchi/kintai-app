"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { useAuth } from "@/context/AuthContext";
import { ReactNode } from "react";
import { SwipeProvider, useSwipeMainHandlers } from "@/context/SwipeContext";

interface NavItem {
  href: string;
  label: string;
  icon: ReactNode;
}

const navItems: NavItem[] = [
  {
    href: "/tutor/works",
    label: "勤務",
    icon: (
      <svg className="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
      </svg>
    ),
  },
  {
    href: "/tutor/templates",
    label: "テンプレート",
    icon: (
      <svg className="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
      </svg>
    ),
  },
  {
    href: "/tutor/salaries",
    label: "給与",
    icon: (
      <svg className="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
      </svg>
    ),
  },
  {
    href: "/tutor/detail",
    label: "マイページ",
    icon: (
      <svg className="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
      </svg>
    ),
  },
];

export default function TutorLayout({ children }: { children: ReactNode }) {
  return (
    <SwipeProvider>
      <TutorLayoutContent>{children}</TutorLayoutContent>
    </SwipeProvider>
  );
}

function TutorLayoutContent({ children }: { children: ReactNode }) {
  const pathname = usePathname();
  const { user } = useAuth();
  const swipeHandlers = useSwipeMainHandlers();

  const isActive = (href: string) => pathname.startsWith(href);

  return (
    <div className="min-h-screen flex flex-col bg-gray-50 dark:bg-gray-900">
      {/* Header */}
      <header className="bg-teal-600 dark:bg-teal-700 fixed top-0 left-0 right-0 z-30">
        <div className="max-w-lg mx-auto px-4 h-14 flex items-center justify-between">
          <span className="text-base font-bold text-white">勤怠管理システム</span>
          {user && (
            <div className="flex flex-col items-end">
              <span className="text-xs text-teal-100 dark:text-teal-800">{user.classroomName}教室</span>
              <span className="text-sm font-semibold text-white">{ user.lastName} {user.firstName}</span>
            </div>
          )}
        </div>
      </header>

      {/* Main content */}
      <main className="flex-1 pt-14 pb-20 max-w-lg mx-auto w-full px-4 overflow-x-hidden" {...swipeHandlers}>
        {children}
      </main>

      {/* Bottom navigation */}
      <nav className="fixed bottom-0 left-0 right-0 bg-white dark:bg-gray-800 border-t border-gray-200 dark:border-gray-700 z-30">
        <div className="max-w-lg mx-auto flex">
          {navItems.map((item) => {
            const active = isActive(item.href);
            return (
              <Link
                key={item.href}
                href={item.href}
                className={`flex-1 flex flex-col items-center py-2 gap-0.5 text-xs transition-colors ${
                  active ? "text-teal-600 dark:text-teal-400" : "text-gray-400 dark:text-gray-500 hover:text-gray-600 dark:hover:text-gray-400"
                }`}
              >
                <span className={active ? "text-teal-600 dark:text-teal-400" : "text-gray-400 dark:text-gray-500"}>
                  {item.icon}
                </span>
                {item.label}
              </Link>
            );
          })}
        </div>
      </nav>
    </div>
  );
}
