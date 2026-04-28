// Shared utility functions
export class Helpers {

  static formatDate(date: Date): string {
    return date.toLocaleDateString();
  }

  static formatCurrency(amount: number): string {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD'
    }).format(amount);
  }

}
