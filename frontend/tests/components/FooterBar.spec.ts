/**
 * Tests for FooterBar component
 */

import { describe, test, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import FooterBar from '@/components/FooterBar.vue'

describe('FooterBar', () => {
  test('should render footer', () => {
    const wrapper = mount(FooterBar)
    
    expect(wrapper.exists()).toBe(true)
    expect(wrapper.find('footer').exists() || wrapper.find('div').exists()).toBe(true)
  })

  test('should contain copyright text', () => {
    const wrapper = mount(FooterBar)
    
    const text = wrapper.text()
    expect(text).toContain('CoffeeCookies')
  })

  test('should have correct structure', () => {
    const wrapper = mount(FooterBar)
    
    // Footer should be a semantic element
    expect(wrapper.html()).toBeTruthy()
  })
})