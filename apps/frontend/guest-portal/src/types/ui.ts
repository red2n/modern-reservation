/**
 * UI state type definitions
 */

export type LoadingState = "idle" | "loading" | "success" | "error";

export type ModalType = "login" | "signup" | "booking" | "gallery" | "confirm";

export type ToastType = "success" | "error" | "warning" | "info";

export interface ModalState {
  isOpen: boolean;
  type?: ModalType;
  data?: unknown;
}

export interface ToastState {
  id: string;
  type: ToastType;
  message: string;
  duration?: number;
}

export interface DrawerState {
  isOpen: boolean;
  content?: React.ReactNode;
}

export interface TabState {
  activeTab: string;
  tabs: Tab[];
}

export interface Tab {
  id: string;
  label: string;
  icon?: React.ReactNode;
  disabled?: boolean;
}

export interface DropdownState {
  isOpen: boolean;
  activeItem?: string;
}

export interface DatePickerState {
  startDate?: Date;
  endDate?: Date;
  focusedInput?: "startDate" | "endDate";
}
