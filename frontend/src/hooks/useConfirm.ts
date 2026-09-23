"use client";

import { useState } from "react";

export function useConfirm<T>() {
  const [target, setTarget] = useState<T | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  return {
    target,
    isLoading,
    setIsLoading,
    confirm: (item: T) => setTarget(item),
    cancel: () => setTarget(null),
  };
}
