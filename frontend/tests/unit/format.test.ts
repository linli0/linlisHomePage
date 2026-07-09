/**
 * Tests for format utility functions
 */

import { describe, it, expect } from 'vitest'
import { formatDate, formatPrice } from '@/utils/format'

describe('format utilities', () => {
  describe('formatDate', () => {
    it('should format ISO date string correctly', () => {
      const result = formatDate('2024-01-15T10:30:00Z')
      expect(result).toBe('2024-01-15')
    })

    it('should format date with different timezone', () => {
      const result = formatDate('2024-12-31T23:59:59+08:00')
      // The exact date depends on local timezone interpretation
      expect(result).toMatch(/^2024-12-\d{2}$/)
    })

    it('should return "--" for undefined input', () => {
      const result = formatDate(undefined)
      expect(result).toBe('--')
    })

    it('should return "--" for empty string', () => {
      const result = formatDate('')
      expect(result).toBe('--')
    })

    it('should handle single digit month correctly', () => {
      const result = formatDate('2024-03-05T00:00:00Z')
      expect(result).toBe('2024-03-05')
    })
  })

  describe('formatPrice', () => {
    it('should format price with two decimal places', () => {
      const result = formatPrice(2350.5)
      expect(result).toBe('2,350.50')
    })

    it('should format large numbers with commas', () => {
      const result = formatPrice(1234567.89)
      expect(result).toBe('1,234,567.89')
    })

    it('should format small numbers correctly', () => {
      const result = formatPrice(10)
      expect(result).toBe('10.00')
    })

    it('should format zero correctly', () => {
      const result = formatPrice(0)
      expect(result).toBe('0.00')
    })

    it('should handle decimal precision', () => {
      const result = formatPrice(100.123)
      expect(result).toBe('100.12')
    })

    it('should format negative numbers', () => {
      const result = formatPrice(-50.25)
      expect(result).toBe('-50.25')
    })
  })
})