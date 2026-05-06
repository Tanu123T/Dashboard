export interface Sprint {
  id: number;
  name: string;
  projectName: string;
  startDate: string;
  endDate: string;
  status: 'completed' | 'active' | 'planned';
  progress: number;
  completedTasks: number;
  totalTasks: number;
  lead?: string;
}

export interface SprintStats {
  totalPlanned: number;
  completed: number;
  active: number;
  avgCompletion: number;
  teamSize: number;
}

export interface TeamMember {
  id: number;
  name: string;
  role: string;
  avatar?: string;
  color?: string;
}
