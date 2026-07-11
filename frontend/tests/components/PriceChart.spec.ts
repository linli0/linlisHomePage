/**
 * Tests for PriceChart component
 */

import { describe, test, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'

// Mock Chart.js before importing
vi.mock('chart.js', () => ({
  Chart: class MockChart {
    static register = vi.fn()
    destroy() {}
    update() {}
  },
  CategoryScale: class {},
  LinearScale: class {},
  PointElement: class {},
  LineElement: class {},
  Title: class {},
  Tooltip: class {},
  Legend: class {},
  Filler: class {},
}))

vi.mock('vue-chartjs', () => ({
  Line: {
    template: '<div class="mock-line-chart"></div>',
    props: ['data', 'options']
  }
}))

// Import after mocking
const PriceChart = await import('@/components/PriceChart.vue').then(m => m.default)

vi.mock('vue-chartjs', () => ({
  Line: {
    template: '<div class="mock-line-chart"></div>',
    props: ['data', 'options']
  }
}))

describe('PriceChart', () => {
  const mockPriceData = [
    { date: '2024-01-01', price: 2300 },
    { date: '2024-01-02', price: 2320 },
    { date: '2024-01-03', price: 2310 },
    { date: '2024-01-04', price: 2350 },
    { date: '2024-01-05', price: 2340 }
  ]

  const defaultProps = {
    data: mockPriceData,
    currency: 'USD',
    symbol: '$'
  }

  test('should render with required props', () => {
    const wrapper = mount(PriceChart, {
      props: defaultProps
    })
    
    expect(wrapper.exists()).toBe(true)
  })

  test('should accept data prop', () => {
    const wrapper = mount(PriceChart, {
      props: defaultProps
    })
    
    expect(wrapper.props('data')).toEqual(mockPriceData)
  })

  test('should accept currency prop', () => {
    const wrapper = mount(PriceChart, {
      props: defaultProps
    })
    
    expect(wrapper.props('currency')).toBe('USD')
  })

  test('should accept symbol prop', () => {
    const wrapper = mount(PriceChart, {
      props: defaultProps
    })
    
    expect(wrapper.props('symbol')).toBe('$')
  })

  test('should handle empty data', () => {
    const wrapper = mount(PriceChart, {
      props: {
        data: [],
        currency: 'USD',
        symbol: '$'
      }
    })
    
    expect(wrapper.exists()).toBe(true)
  })

  test('should handle different currencies', () => {
    const wrapper = mount(PriceChart, {
      props: {
        data: mockPriceData,
        currency: 'CNY',
        symbol: '¥'
      }
    })
    
    expect(wrapper.props('currency')).toBe('CNY')
    expect(wrapper.props('symbol')).toBe('¥')
  })

  test('should update when data changes', async () => {
    const wrapper = mount(PriceChart, {
      props: defaultProps
    })
    
    const newData = [
      { date: '2024-02-01', price: 2400 },
      { date: '2024-02-02', price: 2450 }
    ]
    
    await wrapper.setProps({ data: newData })
    
    expect(wrapper.props('data')).toEqual(newData)
  })
})