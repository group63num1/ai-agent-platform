import { apps as mock } from '@/mock/apps'
import { http, USE_MOCK } from '@/utils/http'

function normalize(app) {
  return {
    ...app,
    price: Number(app.price ?? 0),
    rating: Number(app.rating ?? 0),
    downloads: Number(app.downloads ?? 0),
    reviews: Number(app.reviews ?? 0),
  }
}

export async function getApps(sortBy = '', sortOrder = 'desc') {
  if (USE_MOCK) {
    let result = [...mock]
    
    if (sortBy === 'rating') {
      result.sort((a, b) => sortOrder === 'asc' 
        ? a.rating - b.rating 
        : b.rating - a.rating
      )
    } else if (sortBy === 'downloads') {
      result.sort((a, b) => sortOrder === 'asc' 
        ? a.downloads - b.downloads 
        : b.downloads - a.downloads
      )
    }
    
    return result.map(normalize)
  }

  // 后端请求，添加排序参数
  const params = {}
  if (sortBy) {
    params.sortBy = sortBy
    params.sortOrder = sortOrder
  }
  
  const list = await http.get('/apps', { params })
  return (Array.isArray(list) ? list : []).map(normalize)
}