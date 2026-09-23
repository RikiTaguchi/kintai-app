"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";

export default function TutorEditPage() {
  const router = useRouter();
  useEffect(() => { router.replace("/tutor/detail"); }, [router]);
  return null;
}
