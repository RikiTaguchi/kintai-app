"use client";

import { usePathname } from "next/navigation";
import TutorLayout from "@/components/layout/TutorLayout";
import { ReactNode } from "react";

const PUBLIC_PATHS = ["/tutor/login"];

export default function TutorChrome({ children }: { children: ReactNode }) {
  const pathname = usePathname();
  if (PUBLIC_PATHS.includes(pathname)) return <>{children}</>;
  return <TutorLayout>{children}</TutorLayout>;
}
