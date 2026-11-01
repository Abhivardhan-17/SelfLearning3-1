
import { useState } from 'react'
import {
  Search as SearchIcon,
  UserRound,
  ArrowLeft,
  Grid3X3
} from 'lucide-react'
import api from './api'

export default function Search() {
  const [query, setQuery] = useState('')
  const [users, setUsers] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const [selectedProfile, setSelectedProfile] = useState(null)
  const [profileLoading, setProfileLoading] = useState(false)
  const [profileError, setProfileError] = useState('')

  const [followStatus, setFollowStatus] = useState(null)
  const [followLoading, setFollowLoading] = useState(false)
  const [followError, setFollowError] = useState('')

  const searchUsers = async value => {
    setQuery(value)
    setError('')

    if (!value.trim()) {
      setUsers([])
      return
    }

    setLoading(true)

    try {
      const { data } = await api.get(
        `/users/search?q=${encodeURIComponent(value)}`
      )
      setUsers(data)
    } catch {
      setError('Could not search users. Please try again.')
      setUsers([])
    } finally {
      setLoading(false)
    }
  }

  const openProfile = async id => {
    setProfileLoading(true)
    setProfileError('')
    setFollowError('')
    setSelectedProfile(null)
    setFollowStatus(null)

    try {
      const { data } = await api.get(`/users/${id}`)
      setSelectedProfile(data)

      try {
        const statusResponse = await api.get(`/follows/${id}/status`)
        setFollowStatus(statusResponse.data)
      } catch {
        setFollowError('Could not load follow information.')
      }
    } catch {
      setProfileError('Could not load this profile. Please try again.')
    } finally {
      setProfileLoading(false)
    }
  }

  const toggleFollow = async () => {
    if (!selectedProfile || !followStatus || followLoading) return

    setFollowLoading(true)
    setFollowError('')

    try {
      if (followStatus.following) {
        await api.delete(`/follows/${selectedProfile.id}`)

        setFollowStatus(previous => ({
          ...previous,
          following: false,
          followersCount: Math.max(0, previous.followersCount - 1)
        }))
      } else {
        await api.post(`/follows/${selectedProfile.id}`)

        setFollowStatus(previous => ({
          ...previous,
          following: true,
          followersCount: previous.followersCount + 1
        }))
      }
    } catch {
      setFollowError('Could not update follow status. Please try again.')
    } finally {
      setFollowLoading(false)
    }
  }

  const backToSearch = () => {
    setSelectedProfile(null)
    setFollowStatus(null)
    setProfileError('')
    setFollowError('')
  }

  if (profileLoading) {
    return (
      <main className="profile-page">
        <p className="muted center">Loading profile...</p>
      </main>
    )
  }

  if (profileError) {
    return (
      <main className="profile-page">
        <button
          className="notification-action"
          onClick={backToSearch}
        >
          <ArrowLeft size={17} /> Back to search
        </button>
        <p className="error">{profileError}</p>
      </main>
    )
  }

  if (selectedProfile) {
    return (
      <main className="profile-page">
        <button
          className="notification-action"
          onClick={backToSearch}
        >
          <ArrowLeft size={17} /> Back to search
        </button>

        <div className="profile-header">
          <div className="avatar profile-avatar">
            {selectedProfile.username?.[0]?.toUpperCase() || (
              <UserRound />
            )}
          </div>

          <div>
            <h2>{selectedProfile.username}</h2>

            <div className="profile-counts">
              <span>
                <b>{selectedProfile.postsCount}</b> posts
              </span>

              {followStatus && (
                <>
                  <span>
                    <b>{followStatus.followersCount}</b> followers
                  </span>
                  <span>
                    <b>{followStatus.followingCount}</b> following
                  </span>
                </>
              )}
            </div>

            <p>
              {selectedProfile.bio || 'Welcome to my KLMedia profile.'}
            </p>

            {followStatus && (
              <button
                type="button"
                className="primary"
                onClick={toggleFollow}
                disabled={followLoading}
              >
                {followLoading
                  ? 'Please wait...'
                  : followStatus.following
                    ? 'Unfollow'
                    : 'Follow'}
              </button>
            )}

            {followError && (
              <p className="error">{followError}</p>
            )}
          </div>
        </div>

        <div className="profile-tabs">
          <Grid3X3 size={18} /> &nbsp; POSTS
        </div>

        <div className="profile-grid">
          {selectedProfile.posts?.map(post => (
            <div key={post.id} className="profile-tile">
              {post.imageUrl ? (
                <img src={post.imageUrl} alt="User post" />
              ) : (
                <div className="tile-caption">
                  {post.caption}
                </div>
              )}
            </div>
          ))}
        </div>

        {selectedProfile.posts?.length === 0 && (
          <div className="empty">
            <Grid3X3 size={34} />
            <h3>No posts yet</h3>
            <p>This user hasn't shared any posts.</p>
          </div>
        )}
      </main>
    )
  }

  return (
    <main className="search-page">
      <div className="search-header">
        <h2>Discover People</h2>
        <p className="muted">
          Find people and explore your community.
        </p>
      </div>

      <div className="search-input-box">
        <SearchIcon size={21} />
        <input
          type="text"
          placeholder="Search by username..."
          value={query}
          onChange={e => searchUsers(e.target.value)}
        />
      </div>

      {loading && (
        <p className="muted center">Searching...</p>
      )}

      {error && <p className="error">{error}</p>}

      {!loading && query.trim() && users.length === 0 && !error && (
        <div className="empty">
          <SearchIcon size={34} />
          <h3>No users found</h3>
          <p>Try searching with another username.</p>
        </div>
      )}

      <div className="search-results">
        {users.map(person => (
          <button
            type="button"
            className="search-user-card"
            key={person.id}
            onClick={() => openProfile(person.id)}
          >
            <div className="avatar">
              {person.username?.[0]?.toUpperCase() || (
                <UserRound />
              )}
            </div>

            <div className="search-user-info">
              <b>{person.username}</b>
              <small>{person.bio || 'No bio available'}</small>
              <small>{person.postsCount} posts</small>
            </div>
          </button>
        ))}
      </div>
    </main>
  )
}
