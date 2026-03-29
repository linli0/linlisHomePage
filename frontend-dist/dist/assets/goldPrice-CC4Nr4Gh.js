import{A as e}from"./index-BrDwMkeR.js";const i={getCurrentPrice:(r="USD")=>e.get(`/gold-price/current?currency=${r}`),getPriceHistory:(r="USD",c=30)=>e.get(`/gold-price/history?currency=${r}&days=${c}`),getSupportedCurrencies:()=>e.get("/gold-price/currencies")};export{i as g};
//# sourceMappingURL=goldPrice-CC4Nr4Gh.js.map
